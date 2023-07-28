package com.asm.controller.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.asm.bean.Brand;
import com.asm.service.BrandService;
import com.asm.service.UploadService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@CrossOrigin(origins = "*")
public class UploadRestController {
	@Autowired
	UploadService uService;

	@PostMapping("/admin/rest/upload/{folder}")
	/*
	 * @PathVariable("folder") String folder: Annotation này liên kết giá trị của biến đường dẫn {folder} với tham số folder 
	 * Nó đại diện cho thư mục đích nơi file được tải lên sẽ được lưu trữ.
	 * @PathParam("file") MultipartFile file: Annotation này liên kết giá trị của biến đường dẫn {file} với tham số file
	 * Nó đại diện cho file được tải lên.
	 */
	public JsonNode upload(@PathVariable("folder") String folder, @PathParam("file") MultipartFile file) {
		// lưu  file tải lên vào thư mục folder
		File saveFile = uService.save(file, folder);
		ObjectMapper mapper = new ObjectMapper(); // Tạo một đối tượng ObjectMapper để chuyển đổi dữ liệu sang định dạng json
		ObjectNode node = mapper.createObjectNode(); //  Tạo một đối tượng ObjectNode để tạo nút json
		// Thêm một cặp khóa-giá trị khác vào đối tượng node, trong đó "size" là khóa và saveFile.length() là giá trị
		node.put("filename", saveFile.getName());
		node.put("size", saveFile.length()); // kích thước file được lưu trữ
		return node;
	}
		
		@PostMapping("/admin/rest/upload/product/{folder}")
		/*
		 * @PathVariable("folder") String folder: Annotation này liên kết giá trị của biến đường dẫn {folder} với tham số folder
		 * Nó đại diện cho thư mục đích nơi các file được tải lên sẽ được lưu trữ.
		 * @PathParam("files") MultipartFile[] files: Annotation này liên kết giá trị của biến đường dẫn {files} với tham số files
		 * Nó đại diện cho danh sách các file được tải lên.
		 */
		public List<JsonNode> upload(@PathVariable("folder") String folder, @PathParam("files") MultipartFile[] files) {
			//Khởi tạo một danh sách rỗng listJson để chứa thông tin về các file đã tải lên dưới dạng json
			List<JsonNode> listJson = new ArrayList<JsonNode>();
			List<File> listFile = uService.save(files, folder); // lưu trữ danh sách các file được tải lên vào thư mục đích folder
			for(File saveFile : listFile) {
				// Tạo một đối tượng ObjectMapper để chuyển đổi dữ liệu sang định dạng json
				ObjectMapper mapper = new ObjectMapper();
				//Tạo một đối tượng ObjectNode để tạo nút json
				ObjectNode node = mapper.createObjectNode();
				// Thêm một cặp khóa-giá trị khác vào đối tượng node, trong đó "size" là khóa và saveFile.length() là giá trị
				node.put("filename", saveFile.getName());
				node.put("size", saveFile.length()); // kích thước file được lưu trữ
				listJson.add(node); //Thêm đối tượng node vào danh sách listJson
			}
			System.out.println(listJson);
			return listJson;
		}
	
}

