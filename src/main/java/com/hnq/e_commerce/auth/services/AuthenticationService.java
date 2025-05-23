package com.hnq.e_commerce.auth.services;

import com.hnq.e_commerce.auth.constant.PredefinedRole;
import com.hnq.e_commerce.auth.dto.request.*;
import com.hnq.e_commerce.auth.dto.response.AuthenticationResponse;
import com.hnq.e_commerce.auth.dto.response.IntrospectResponse;
import com.hnq.e_commerce.auth.entities.InvalidatedToken;
import com.hnq.e_commerce.auth.entities.Role;
import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.auth.repositories.InvalidatedTokenRepository;
import com.hnq.e_commerce.auth.repositories.RoleRepository;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.mapper.UserMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    private final EmailService emailService;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (ResourceNotFoundEx e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request,
                                               HttpServletResponse response) throws ParseException, JOSEException
    {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new ResourceNotFoundEx(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);

        var refreshToken = generateRefreshToken(user);

        return AuthenticationResponse.builder().id(user.getId()).email(user.getEmail())
                .accessToken(token).refreshToken(refreshToken).authenticated(true).build();
    }

    public AuthenticationResponse verifyOrCreateUser(OAuthRegistrationRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            var token = generateToken(existingUser.get());
            return AuthenticationResponse.builder().id(existingUser.get().getId()).email(existingUser.get().getEmail()).accessToken(token).authenticated(true).build();
        } else {
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setFirstName(request.getName());
            newUser.setLastName(request.getName());
            newUser.setCreatedOn(new Date());
            newUser.setUpdatedOn(new Date());
            newUser.setEnabled(true);
            newUser.setProvider(request.getProvider());
            newUser.setImageUrl(request.getImage());
            HashSet<Role> roles = new HashSet<>();
            roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
            newUser.setRoles(roles);
            try {
                User nUser = userRepository.save(newUser);
                var token = generateToken(nUser);
                return AuthenticationResponse.builder().id(nUser.getId()).email(nUser.getEmail())
                        .accessToken(token).authenticated(true).build();
            } catch (DataIntegrityViolationException exception) {
                throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (ResourceNotFoundEx exception) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByEmail(username).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);
        var refreshToken = generateRefreshToken(user);
        return AuthenticationResponse.builder().accessToken(token).refreshToken(refreshToken).authenticated(true).build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("hnq")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("hnq")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {

            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                                   .getJWTClaimsSet()
                                   .getIssueTime()
                                   .toInstant()
                                   .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                                   .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new ResourceNotFoundEx(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new ResourceNotFoundEx(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResourceNotFoundEx(ErrorCode.PASSWORD_NOT_MATCH);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedOn(new Date());
        userRepository.save(user);
    }

    public void forgetPassword(ForgetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));
        String password = generateRandomString(8);
        user.setPassword(passwordEncoder.encode(password));
        emailService.sendPassword(user, password);
        user.setUpdatedOn(new Date());
        userRepository.save(user);
    }
    @EventListener(ContextRefreshedEvent.class)
//    @Scheduled(cron = "0 0 0 * * ?")  //gọi method vào 0h mỗi ngày
    @Transactional
    public void deleteExpiredTokens() {
        Date now = new Date();
        Date thresholdDate = Date.from(Instant.now().minus(10, ChronoUnit.DAYS));
        List<InvalidatedToken> expiredTokens = invalidatedTokenRepository.findByExpiryTimeBefore(thresholdDate);

        if (expiredTokens.isEmpty()) {
            return;
        }

        invalidatedTokenRepository.deleteByExpiryTimeBefore(thresholdDate);
    }
}