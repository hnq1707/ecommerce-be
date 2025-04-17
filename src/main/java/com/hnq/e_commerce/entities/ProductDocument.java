package com.hnq.e_commerce.entities.elasticsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.Date;

@Document(indexName = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Float)
    private Float rating;

    @Field(type = FieldType.Keyword)
    private String thumbnail;

    @Field(type = FieldType.Boolean)
    private boolean isNewArrival;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Date)
    private Date createdAt;

    @Field(type = FieldType.Date)
    private Date updatedAt;

    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private String categoryName;

    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private String categoryTypeName;
}