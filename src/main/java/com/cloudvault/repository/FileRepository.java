package com.cloudvault.repository;

import com.cloudvault.model.FileMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRepository extends MongoRepository<FileMetadata, String> {
    
    List<FileMetadata> findByUserId(String userId);
    
    List<FileMetadata> findByUserIdAndFileType(String userId, String fileType);
    
    List<FileMetadata> findByUserIdOrderByUploadDateDesc(String userId);
}