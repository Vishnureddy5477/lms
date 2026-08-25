package com.cranesvarsity.template.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Port of the legacy model.S3Uploader — same bucket, same key format
 * ({token}-{timestamp}-{filename}), same public-read ACL, same
 * media.cranesvarsitycrm.com URL domain — so existing tooling/links that
 * assume that URL shape keep working. Credentials come from env vars, not
 * hardcoded like the legacy class.
 */
@Service
public class S3UploadService {

    private static final String S3_DOMAIN = "https://media.cranesvarsitycrm.com";

    private final String bucket;
    private final Regions region;
    private final String accessKey;
    private final String secretKey;

    public S3UploadService(@Value("${app.s3.bucket}") String bucket,
                            @Value("${app.s3.region}") String region,
                            @Value("${app.s3.access-key}") String accessKey,
                            @Value("${app.s3.secret-key}") String secretKey) {
        this.bucket = bucket;
        this.region = Regions.fromName(region);
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String upload(MultipartFile file, String folderName) throws IOException {
        AmazonS3 s3Client = buildClient();

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        String sanitizedName = originalName.replaceAll("\\s+", "");

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");

        String token = generateSmallLetterToken(5);
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String newFileName = token + "-" + timestamp + "-" + sanitizedName;
        String s3Key = folderName + "/" + newFileName;

        PutObjectRequest request = new PutObjectRequest(bucket, s3Key, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        s3Client.putObject(request);

        return S3_DOMAIN + "/" + s3Key;
    }

    /** Port of the legacy model.S3FileDeleter — same domain-stripping key derivation. */
    public boolean delete(String docUrl) {
        if (docUrl == null || !docUrl.contains("media.cranesvarsitycrm.com/")) {
            return false;
        }
        try {
            String marker = "media.cranesvarsitycrm.com/";
            String objectKey = docUrl.substring(docUrl.indexOf(marker) + marker.length());
            buildClient().deleteObject(bucket, objectKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private AmazonS3 buildClient() {
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTPS);
        clientConfig.setConnectionTimeout(60000);
        clientConfig.setSocketTimeout(60000);
        clientConfig.setMaxErrorRetry(3);

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .withClientConfiguration(clientConfig)
                .build();
    }

    private String generateSmallLetterToken(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < length; i++) {
            token.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return token.toString();
    }
}
