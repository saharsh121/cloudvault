package com.cloudvault.controller;

import com.cloudvault.model.FileMetadata;
import com.cloudvault.model.User;
import com.cloudvault.repository.FileRepository;
import com.cloudvault.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FileController {

@Autowired
private CloudinaryService cloudinaryService;

@Autowired
private FileRepository fileRepository;

@PostMapping("/upload")
public Map<String, Object> uploadFile(
@RequestParam("files") MultipartFile[] files,
@RequestParam("type") String type,
HttpSession session) {

Map<String, Object> response = new HashMap<>();

try {

User currentUser = (User) session.getAttribute("user");

if (currentUser == null) {
response.put("success", false);
response.put("message", "User not logged in!");
return response;
}

if (files == null || files.length == 0) {
response.put("success", false);
response.put("message", "No files provided");
return response;
}
System.out.println("UPLOAD REQUEST");
System.out.println("User: " + currentUser.getUserId());
System.out.println("Type: " + type);
System.out.println("Files: " + files.length);

for (MultipartFile file : files) {

if (file.isEmpty()) {
System.out.println("Skipping empty file");
continue;
}

String fileType = determineFileType(file, type);

System.out.println("File: " + file.getOriginalFilename());
System.out.println("File Type: " + fileType);
System.out.println("MIME Type: " + file.getContentType());

Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, fileType);

String cloudinaryUrl = (String) uploadResult.get("secure_url");
String cloudinaryPublicId = (String) uploadResult.get("public_id");

System.out.println("CLOUDINARY RESULT");
System.out.println("URL: " + cloudinaryUrl);
System.out.println("Public ID: " + cloudinaryPublicId);
System.out.println("Resource Type: " + uploadResult.get("resource_type"));
System.out.println("Format: " + uploadResult.get("format"));

if (cloudinaryUrl == null || cloudinaryUrl.trim().isEmpty()) {
throw new RuntimeException("Cloudinary did not return a secure URL");
}

if (cloudinaryPublicId == null || cloudinaryPublicId.trim().isEmpty()) {
throw new RuntimeException("Cloudinary did not return a public ID");
}

FileMetadata metadata = new FileMetadata();

metadata.setUserId(currentUser.getUserId());
metadata.setFileName(file.getOriginalFilename());
metadata.setOriginalName(file.getOriginalFilename());
metadata.setFileType(fileType);
metadata.setMimeType(file.getContentType());
metadata.setFileSize(file.getSize());
metadata.setCloudinaryUrl(cloudinaryUrl);
metadata.setCloudinaryPublicId(cloudinaryPublicId);
metadata.setUploadDate(LocalDateTime.now());

fileRepository.save(metadata);

System.out.println("MongoDB metadata saved");
}

response.put("success", true);
response.put("message", files.length + " file(s) uploaded successfully!");

} catch (Exception e) {

e.printStackTrace();

response.put("success", false);
response.put("message", "Upload failed: " + e.getMessage());
}

return response;
}

@GetMapping("/api/files")
public List<FileMetadata> getFiles(
@RequestParam("type") String type,
HttpSession session) {

User currentUser = (User) session.getAttribute("user");

if (currentUser == null) {
return Collections.emptyList();
}

String fileType = type;

if (type.endsWith("s")) {
fileType = type.substring(0, type.length() - 1);
}

return fileRepository.findByUserIdAndFileType(currentUser.getUserId(), fileType);
}

@DeleteMapping("/api/files/{id}")
public Map<String, Object> deleteFile(
@PathVariable String id,
HttpSession session) {

Map<String, Object> response = new HashMap<>();

try {

User currentUser = (User) session.getAttribute("user");

if (currentUser == null) {
response.put("success", false);
response.put("message", "User not logged in!");
return response;
}

FileMetadata file = fileRepository.findById(id).orElse(null);

if (file == null) {
response.put("success", false);
response.put("message", "File not found!");
return response;
}

if (!file.getUserId().equals(currentUser.getUserId())) {
response.put("success", false);
response.put("message", "You don't have permission to delete this file!");
return response;
}

String publicId = file.getCloudinaryPublicId();

if (publicId != null && !publicId.trim().isEmpty()) {
cloudinaryService.deleteFile(publicId, file.getFileType());
}

fileRepository.deleteById(id);

response.put("success", true);
response.put("message", "File deleted successfully!");

} catch (Exception e) {

e.printStackTrace();

response.put("success", false);
response.put("message", "Delete failed: " + e.getMessage());
}

return response;
}

private String determineFileType(MultipartFile file, String type) {

String contentType = file.getContentType();

if (contentType != null) {

if (contentType.startsWith("image/")) {
return "image";
}

if (contentType.startsWith("video/")) {
return "video";
}

if (contentType.startsWith("audio/")) {
return "audio";
}

if (contentType.equals("application/pdf")
|| contentType.startsWith("application/msword")
|| contentType.startsWith("application/vnd.openxmlformats-officedocument")
|| contentType.equals("text/plain")
|| contentType.startsWith("application/vnd.ms-excel")
|| contentType.startsWith("application/vnd.ms-powerpoint")) {
return "document";
}
}

if (type != null && type.endsWith("s")) {
return type.substring(0, type.length() - 1);
}

return type;
}
}