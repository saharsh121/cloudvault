console.log("Dashboard JavaScript loaded!");

function showUpload(type) {
console.log("Showing upload for:", type);
document.querySelectorAll('.upload-section').forEach(function(el) {
el.classList.remove('active');
});
const uploadOptions = document.getElementById('uploadOptions');
if (uploadOptions) {
uploadOptions.style.display = 'none';
}
const section = document.getElementById('upload-' + type);
if (section) {
section.classList.add('active');
} else {
console.error("Upload section not found:", 'upload-' + type);
}
}

function hideUpload(type) {
console.log("Hiding upload for:", type);
const section = document.getElementById('upload-' + type);
if (section) {
section.classList.remove('active');
}
const uploadOptions = document.getElementById('uploadOptions');
if (uploadOptions) {
uploadOptions.style.display = 'grid';
}
}

function updateFileStatus(type) {
console.log("updateFileStatus called for:", type);
const inputIds = {
images: 'imagesInput',
videos: 'videosInput',
documents: 'documentsInput',
audio: 'audioInput'
};
const statusIds = {
images: 'imagesStatus',
videos: 'videosStatus',
documents: 'documentsStatus',
audio: 'audioStatus'
};
const inputId = inputIds[type];
const statusId = statusIds[type];
console.log("Looking for input:", inputId);
console.log("Looking for status:", statusId);
const input = document.getElementById(inputId);
const status = document.getElementById(statusId);
console.log("Input element:", input);
console.log("Status element:", status);
if (!input) {
console.error("Input element not found:", inputId);
return;
}
if (!status) {
console.error("Status element not found:", statusId);
return;
}
console.log("Files selected:", input.files);
if (input.files && input.files.length > 0) {
let names = [];
let totalSize = 0;
for (let i = 0; i < input.files.length; i++) {
names.push(input.files[i].name);
totalSize += input.files[i].size;
console.log("File " + (i + 1) + ":", input.files[i].name);
}
let displayName;
if (names.length > 3) {
displayName = names.slice(0, 3).join(', ') + ' and ' + (names.length - 3) + ' more';
} else {
displayName = names.join(', ');
}
status.textContent = 'Selected: ' + displayName + ' (' + formatFileSize(totalSize) + ')';
status.style.color = '#2d3436';
status.style.fontWeight = '600';
console.log("File selection displayed successfully");
} else {
status.textContent = 'No ' + type + ' selected';
status.style.color = '#6c757d';
status.style.fontWeight = 'normal';
}
}

function uploadFiles(type) {
console.log("Upload function called for:", type);
const inputIds = {
images: 'imagesInput',
videos: 'videosInput',
documents: 'documentsInput',
audio: 'audioInput'
};
const statusIds = {
images: 'imagesStatus',
videos: 'videosStatus',
documents: 'documentsStatus',
audio: 'audioStatus'
};
const inputId = inputIds[type];
console.log("Looking for upload input:", inputId);
const input = document.getElementById(inputId);
if (!input) {
console.error("Input element not found:", inputId);
alert('Error: Input element not found: ' + inputId);
return;
}
console.log("Files selected:", input.files);
console.log("Number of files:", input.files ? input.files.length : 0);
if (!input.files || input.files.length === 0) {
alert('Please select at least one file to upload!');
return;
}
const formData = new FormData();
for (let i = 0; i < input.files.length; i++) {
console.log("Adding file:", input.files[i].name);
formData.append('files', input.files[i]);
}
formData.append('type', type);
console.log("FormData created");
console.log("File type:", type);
const btn = document.querySelector('#upload-' + type + ' .upload-btn');
if (!btn) {
console.error("Upload button not found for:", type);
alert('Error: Upload button not found!');
return;
}
const originalText = btn.innerHTML;
btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';
btn.disabled = true;
console.log("Sending files to /upload");
fetch('/upload', {
method: 'POST',
body: formData
})
.then(function(response) {
console.log("Response status:", response.status);
if (!response.ok) {
throw new Error('Server error: ' + response.status);
}
return response.json();
})
.then(function(data) {
console.log("Response data:", data);
btn.innerHTML = originalText;
btn.disabled = false;
if (data.success) {
alert(data.message || 'Files uploaded successfully!');
input.value = '';
const status = document.getElementById(statusIds[type]);
if (status) {
status.textContent = 'No ' + type + ' selected';
status.style.color = '#6c757d';
status.style.fontWeight = 'normal';
}
loadFiles(type);
} else {
alert(data.message || 'Upload failed!');
}
})
.catch(function(error) {
console.error("Upload error:", error);
btn.innerHTML = originalText;
btn.disabled = false;
alert('Error: ' + error.message);
});
}

function showView(type) {
console.log("Showing view for:", type);
document.querySelectorAll('.view-section').forEach(function(el) {
el.classList.remove('active');
});
const viewOptions = document.getElementById('viewOptions');
if (viewOptions) {
viewOptions.style.display = 'none';
}
const section = document.getElementById('view-' + type);
if (section) {
section.classList.add('active');
} else {
console.error("View section not found:", 'view-' + type);
return;
}
loadFiles(type);
}

function hideView(type) {
console.log("Hiding view for:", type);
const section = document.getElementById('view-' + type);
if (section) {
section.classList.remove('active');
}
const viewOptions = document.getElementById('viewOptions');
if (viewOptions) {
viewOptions.style.display = 'grid';
}
}

function loadFiles(type) {
console.log("Loading files for type:", type);
fetch('/api/files?type=' + encodeURIComponent(type))
.then(function(response) {
console.log("Files response:", response.status);
if (!response.ok) {
throw new Error('Server error: ' + response.status);
}
return response.json();
})
.then(function(data) {
console.log("Files received:", data);
const gridIds = {
images: 'imageGrid',
videos: 'videoGrid',
documents: 'documentGrid',
audio: 'audioGrid'
};
const gridId = gridIds[type];
const grid = document.getElementById(gridId);
if (!grid) {
console.error("Grid element not found:", gridId);
return;
}
if (!data || data.length === 0) {
grid.innerHTML = `
<div class="empty-state">
<i class="fas fa-cloud-upload-alt"></i>
No ${type} uploaded yet
</div>
`;
return;
}
let html = '';
data.forEach(function(file) {
html += `
<div class="file-item">
${getFilePreview(file)}
<div class="file-name">
${escapeHtml(file.fileName)}
</div>
<div class="file-size">
${formatFileSize(file.fileSize)}
</div>
<button
type="button"
class="delete-btn"
onclick="deleteFile('${file.id}')">
<i class="fas fa-trash"></i>
Delete
</button>
</div>
`;
});
grid.innerHTML = html;
})
.catch(function(error) {
console.error('Error loading files:', error);
const gridIds = {
images: 'imageGrid',
videos: 'videoGrid',
documents: 'documentGrid',
audio: 'audioGrid'
};
const grid = document.getElementById(gridIds[type]);
if (grid) {
grid.innerHTML = `
<div
class="empty-state"
style="color: #dc3545;">
<i class="fas fa-exclamation-circle"></i>
Error loading files:
${escapeHtml(error.message)}
</div>
`;
}
});
}

function getFilePreview(file) {
if (file.fileType === 'image') {
return `
<img
src="${file.url}"
alt="${escapeHtml(file.fileName)}"
style="
width: 100%;
height: 150px;
object-fit: cover;
border-radius: 8px;"
onerror="
this.src =
'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22150%22%3E%3Crect width=%22200%22 height=%22150%22 fill=%22%23f0f0f0%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 fill=%22%23999%22 font-size=%2214%22%3ENo Image%3C/text%3E%3C/svg%3E'
">
`;
}
else if (file.fileType === 'video') {
return `
<video
src="${file.url}"
controls
style="
width: 100%;
height: 150px;
object-fit: cover;
border-radius: 8px;">
</video>
`;
}
else if (file.fileType === 'audio') {
return `
<div class="audio-wrapper">
<i class="fas fa-music"></i>
<audio
controls
style="
width: 100%;
margin-top: 10px;">
<source src="${file.url}">
Your browser does not support
audio playback.
</audio>
</div>
`;
}
else {
return `
<i class="fas fa-file-alt file-icon"></i>
`;
}
}

function formatFileSize(bytes) {
if (!bytes || bytes === 0) {
return '0 B';
}
if (bytes < 1024) {
return bytes + ' B';
}
if (bytes < 1024 * 1024) {
return (bytes / 1024).toFixed(1) + ' KB';
}
if (bytes < 1024 * 1024 * 1024) {
return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}
return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
}

function deleteFile(fileId) {
if (!confirm('Are you sure you want to delete this file?')) {
return;
}
console.log("Deleting file:", fileId);
fetch('/api/files/' + encodeURIComponent(fileId), {
method: 'DELETE'
})
.then(function(response) {
console.log("Delete response:", response.status);
if (!response.ok) {
throw new Error('Server error: ' + response.status);
}
return response.json();
})
.then(function(data) {
console.log("Delete response data:", data);
if (data.success) {
alert('File deleted successfully!');
const activeView = document.querySelector('.view-section.active');
if (activeView) {
const type = activeView.id.replace('view-', '');
loadFiles(type);
}
} else {
alert(data.message || 'Could not delete file.');
}
})
.catch(function(error) {
console.error("Delete error:", error);
alert('Error: ' + error.message);
});
}

function escapeHtml(value) {
if (value === null || value === undefined) {
return '';
}
return String(value)
.replace(/&/g, '&amp;')
.replace(/</g, '&lt;')
.replace(/>/g, '&gt;')
.replace(/"/g, '&quot;')
.replace(/'/g, '&#039;');
}

document.addEventListener('DOMContentLoaded', function() {
console.log("Dashboard DOM loaded");
console.log("Checking file inputs...");
console.log("Images input:", document.getElementById('imagesInput'));
console.log("Videos input:", document.getElementById('videosInput'));
console.log("Documents input:", document.getElementById('documentsInput'));
console.log("Audio input:", document.getElementById('audioInput'));
});