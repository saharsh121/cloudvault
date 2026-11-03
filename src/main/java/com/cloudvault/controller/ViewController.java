package com.cloudvault.controller;

import com.cloudvault.model.FileMetadata;
import com.cloudvault.model.User;
import com.cloudvault.repository.FileRepository;
import com.cloudvault.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Controller
public class ViewController {

@Autowired
private FileRepository fileRepository;

@Autowired
private CloudinaryService cloudinaryService;

@GetMapping("/view/{type}")
public String viewFiles(
@PathVariable String type,
HttpSession session,
Model model) {

System.out.println("VIEW FILES");
System.out.println("Type: " + type);

User currentUser = (User) session.getAttribute("user");

if (currentUser == null) {
System.out.println("User not logged in");
return "redirect:/login";
}

System.out.println("User: " + currentUser.getUserId() + " - " + currentUser.getName());

String fileType = type;

if (type.endsWith("s")) {
fileType = type.substring(0, type.length() - 1);
}

System.out.println("Searching for file type: " + fileType);

List<FileMetadata> files = fileRepository.findByUserIdAndFileType(
currentUser.getUserId(),
fileType
);

System.out.println("Found " + files.size() + " files");

model.addAttribute("files", files);
model.addAttribute("type", type);
model.addAttribute("typeDisplay", type.substring(0, 1).toUpperCase() + type.substring(1));
model.addAttribute("fileCount", files.size());

return "view-" + type;
}

@GetMapping("/download/{id}")
public ResponseEntity<InputStreamResource> downloadFile(
@PathVariable String id,
HttpSession session) {

System.out.println("DOWNLOAD FILE");
System.out.println("File ID: " + id);

try {

User currentUser = (User) session.getAttribute("user");

if (currentUser == null) {
System.out.println("User not logged in");
return ResponseEntity.status(401).build();
}

System.out.println("User: " + currentUser.getUserId());

FileMetadata file = fileRepository.findById(id).orElse(null);

if (file == null) {
System.out.println("File not found in MongoDB");
return ResponseEntity.notFound().build();
}

System.out.println("File: " + file.getOriginalName());

if (!currentUser.getUserId().equals(file.getUserId())) {
System.out.println("Unauthorized download attempt");
return ResponseEntity.status(403).build();
}

String cloudinaryUrl = file.getCloudinaryUrl();

System.out.println("Cloudinary URL: " + cloudinaryUrl);

if (cloudinaryUrl == null || cloudinaryUrl.trim().isEmpty()) {
System.out.println("Cloudinary URL is empty");
return ResponseEntity.notFound().build();
}

URL url = new URL(cloudinaryUrl);

HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setConnectTimeout(10000);
connection.setReadTimeout(30000);
connection.setInstanceFollowRedirects(true);
connection.connect();

int responseCode = connection.getResponseCode();

System.out.println("Cloudinary response: " + responseCode);

if (responseCode != HttpURLConnection.HTTP_OK) {
System.out.println("Cloudinary file unavailable");
return ResponseEntity.status(responseCode).build();
}

InputStream inputStream = connection.getInputStream();
InputStreamResource resource = new InputStreamResource(inputStream);

String fileName = file.getOriginalName();

if (fileName == null || fileName.trim().isEmpty()) {
fileName = "download";
}

MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

if (file.getMimeType() != null && !file.getMimeType().trim().isEmpty()) {
try {
mediaType = MediaType.parseMediaType(file.getMimeType());
} catch (Exception e) {
System.out.println("Invalid MIME type, using default");
}
}

System.out.println("Download started: " + fileName);

return ResponseEntity
.ok()
.contentType(mediaType)
.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
.body(resource);

} catch (Exception e) {

System.out.println("DOWNLOAD ERROR");
e.printStackTrace();

return ResponseEntity.internalServerError().build();
}
}

@PostMapping("/delete/{id}")
public String deleteFile(
@PathVariable String id,
HttpSession session) {

System.out.println("DELETE FILE");
System.out.println("File ID: " + id);

String fileTypeForRedirect = "image";

try {

User currentUser = (User) session.getAttribute("user");

if (currentUser == null) {
System.out.println("User not logged in");
return "redirect:/login";
}

FileMetadata file = fileRepository.findById(id).orElse(null);

if (file == null) {
System.out.println("File not found in MongoDB");
return "redirect:/view/images";
}

System.out.println("File: " + file.getOriginalName());

fileTypeForRedirect = file.getFileType();

if (!currentUser.getUserId().equals(file.getUserId())) {
System.out.println("Unauthorized delete attempt");
return "redirect:/view/images";
}

String publicId = file.getCloudinaryPublicId();

System.out.println("Cloudinary Public ID: " + publicId);

if (publicId != null && !publicId.trim().isEmpty()) {
try {
cloudinaryService.deleteFile(publicId, file.getFileType());
System.out.println("Cloudinary deletion completed");
} catch (Exception e) {
System.out.println("Cloudinary deletion returned an error");
e.printStackTrace();
}
} else {
System.out.println("No Cloudinary Public ID found");
}

fileRepository.delete(file);

System.out.println("MongoDB record deleted");
System.out.println("FILE DELETE COMPLETED");

} catch (Exception e) {
System.out.println("DELETE ERROR");
e.printStackTrace();
}

return "redirect:/view/" + getPluralType(fileTypeForRedirect);
}

private String getPluralType(String fileType) {

if (fileType == null || fileType.trim().isEmpty()) {
return "images";
}

switch (fileType.toLowerCase()) {
case "image":
return "images";
case "video":
return "videos";
case "document":
return "documents";
case "audio":
return "audio";
default:
return fileType.endsWith("s") ? fileType : fileType + "s";
}
}
}