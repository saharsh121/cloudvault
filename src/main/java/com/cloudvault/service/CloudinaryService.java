package com.cloudvault.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;
    public Map<String, Object> uploadFile(
            MultipartFile file,
            String folderName) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty"
            );
        }

        String resourceType =
                getResourceType(folderName);
        System.out.println(" CLOUDINARY UPLOAD");

        System.out.println(
                " File: "
                        + file.getOriginalFilename()
        );

        System.out.println(
                " Folder: cloudvault/"
                        + folderName
        );

        System.out.println(
                " Resource Type: "
                        + resourceType
        );

        System.out.println(
                " MIME Type: "
                        + file.getContentType()
        );

        System.out.println(
                " File Size: "
                        + file.getSize()
                        + " bytes"
        );
        Map<String, Object> uploadResult =
                cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder",
                                "cloudvault/" + folderName,

                                "resource_type",
                                resourceType
                        )
                );
        String publicId =
                (String) uploadResult.get(
                        "public_id"
                );

        String secureUrl =
                (String) uploadResult.get(
                        "secure_url"
                );
        if (publicId == null
                || publicId.trim().isEmpty()) {

            throw new IOException(
                    "Cloudinary did not return public_id"
            );
        }

        if (secureUrl == null
                || secureUrl.trim().isEmpty()) {

            throw new IOException(
                    "Cloudinary did not return secure_url"
            );
        }
        System.out.println("CLOUDINARY UPLOAD SUCCESSFUL");
        System.out.println(
                " Public ID: "
                        + publicId
        );

        System.out.println(
                " Secure URL: "
                        + secureUrl
        );

        System.out.println(
                " Resource Type: "
                        + uploadResult.get(
                                "resource_type"
                        )
        );

        System.out.println(
                " Format: "
                        + uploadResult.get(
                                "format"
                        )
        );
        return uploadResult;
    }
    public void deleteFile(
            String publicId,
            String fileType) throws IOException {

        System.out.println(" CLOUDINARY DELETE");

        System.out.println(
                " Public ID: "
                        + publicId
        );

        System.out.println(
                " File Type: "
                        + fileType
        );
        if (publicId == null
                || publicId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Cloudinary Public ID cannot be empty"
            );
        }
        String resourceType =
                getResourceType(fileType);
        System.out.println(
                "Cloudinary Resource Type: "
                        + resourceType
        );
        Map<String, Object> result =
                cloudinary.uploader().destroy(
                        publicId,
                        ObjectUtils.asMap(
                                "resource_type",
                                resourceType,

                                "type",
                                "upload"
                        )
                );
        System.out.println(
                " Cloudinary response: "
                        + result
        );
    }
    private String getResourceType(
            String type) {

        if (type == null) {
            return "image";
        }
        type = type.toLowerCase().trim();
        if (type.equals("image")
                || type.equals("images")) {

            return "image";
        }
        if (type.equals("video")
                || type.equals("videos")) {

            return "video";
        }
        if (type.equals("document")
                || type.equals("documents")) {

            return "raw";
        }
        if (type.equals("audio")
                || type.equals("audios")) {

            return "video";
        }
        return "raw";
    }
}
