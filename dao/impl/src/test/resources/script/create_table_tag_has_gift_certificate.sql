CREATE TABLE tag_has_gift_certificate
(
    tag_id              BIGINT NOT NULL,
    gift_certificate_id BIGINT NOT NULL,
    PRIMARY KEY (tag_id, gift_certificate_id)
);