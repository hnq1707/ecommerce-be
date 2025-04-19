
package com.hnq.e_commerce.services;

import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.dto.request.AddressRequest;
import com.hnq.e_commerce.entities.Address;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.repositories.AddressRepository;
import com.hnq.e_commerce.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AddressService {

    AddressRepository addressRepository;
    UserRepository userRepository;
    OrderRepository orderRepository;

    /**
     * Tạo địa chỉ mới cho người dùng hiện tại
     *
     * @param addressRequest Thông tin địa chỉ cần tạo
     * @return Đối tượng địa chỉ đã được tạo
     * @throws ResourceNotFoundEx nếu không tìm thấy người dùng
     * @throws IllegalStateException nếu không thể lấy thông tin người dùng từ xác thực
     */
    public Address createAddress(AddressRequest addressRequest) {
        // Xác thực và kiểm tra đầu vào
        if (addressRequest == null) {
            throw new IllegalArgumentException("Thông tin địa chỉ không được để trống");
        }

        String email = getCurrentUserEmail();
        log.info("Tạo địa chỉ mới cho người dùng: {}", email);

        // Tìm người dùng từ email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy người dùng với email: {}", email);
                    return new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED);
                });

        // Tạo đối tượng địa chỉ mới
        Address address = Address.builder()
                .name(addressRequest.getName())
                .street(addressRequest.getStreet())
                .city(addressRequest.getCity())
                .district(addressRequest.getDistrict())
                .zipCode(addressRequest.getZipCode())
                .phoneNumber(addressRequest.getPhoneNumber())
                .user(user)
                .build();

        // Thêm địa chỉ vào danh sách địa chỉ của người dùng
        user.getAddressList().add(address);

        try {
            log.info("Lưu địa chỉ mới");
            return addressRepository.save(address);
        } catch (DataIntegrityViolationException e) {
            log.error("Lỗi lưu địa chỉ: {}", e.getMessage());
            throw new IllegalStateException("Không thể lưu địa chỉ do vi phạm ràng buộc dữ liệu", e);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi lưu địa chỉ: {}", e.getMessage());
            throw new IllegalStateException("Có lỗi xảy ra khi lưu địa chỉ", e);
        }
    }

    /**
     * Xóa địa chỉ theo ID
     *
     * @param id ID của địa chỉ cần xóa
     * @throws ResourceNotFoundEx nếu không tìm thấy địa chỉ
     * @throws AccessDeniedException nếu người dùng không có quyền xóa địa chỉ
     * @throws IllegalArgumentException nếu ID không hợp lệ
     */
    @Transactional
    public void deleteAddress(String id) {
        // Kiểm tra ID
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("ID địa chỉ không được để trống");
        }

        String email = getCurrentUserEmail();
        log.info("Xóa địa chỉ có ID: {} cho người dùng: {}", id, email);

        // Tìm địa chỉ theo ID
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy địa chỉ với ID: {}", id);
                    return new ResourceNotFoundEx(ErrorCode.ADDRESS_NOT_FOUND);
                });

        // Kiểm tra quyền xóa
        if (address.getUser() == null || !address.getUser().getEmail().equals(email)) {
            log.error("Người dùng {} không có quyền xóa địa chỉ {}", email, id);
            throw new AccessDeniedException("Bạn không có quyền xóa địa chỉ này!");
        }

        try {
            // Cập nhật tất cả đơn hàng liên quan về NULL trước khi xóa địa chỉ
            log.info("Cập nhật đơn hàng liên quan đến địa chỉ {}", id);
            orderRepository.updateAddressToNull(id);

            // Xóa địa chỉ
            log.info("Thực hiện xóa địa chỉ {}", id);
            addressRepository.delete(address);
        } catch (Exception e) {
            log.error("Lỗi khi xóa địa chỉ: {}", e.getMessage());
            throw new IllegalStateException("Có lỗi xảy ra khi xóa địa chỉ", e);
        }
    }

    /**
     * Lấy email của người dùng hiện tại từ context xác thực
     *
     * @return Email của người dùng hiện tại
     * @throws IllegalStateException nếu không thể lấy thông tin người dùng
     */
    private String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("Không tìm thấy thông tin xác thực");
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof Jwt)) {
                throw new IllegalStateException("Loại xác thực không được hỗ trợ");
            }

            Jwt jwt = (Jwt) principal;
            return jwt.getClaim("sub");
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin người dùng hiện tại: {}", e.getMessage());
            throw new IllegalStateException("Không thể xác định người dùng hiện tại", e);
        }
    }
}