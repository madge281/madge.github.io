package com.asm.controller.rest;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.asm.bean.Brand;
import com.asm.service.BrandService;
import com.asm.service.UploadService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin/rest/brands")
public class BrandRestController {
	@Autowired
	BrandService bService;
	@Autowired
	UploadService uService;

	@GetMapping("")
	public List<Brand> getAllBrand() {
		return bService.findAll(); // gọi phương thức findAll của bservice để lấy ds các đối tượng brand
	}

	@GetMapping("/{id}")
	public ResponseEntity<Brand> getBrand(@PathVariable("id") String id) {
		if (!bService.existsById(id)) { // ktra brand có tồn tại với id đc cấp hay không
			return ResponseEntity.notFound().build(); // kh tồn tại
		} else {
			return ResponseEntity.ok(bService.findById(id));
		}
	}
	 
	//Phương thức trả về một danh sách Brand tìm thấy hoặc toàn bộ danh sách Brand nếu từ khóa không được cung cấp.
	@GetMapping("/search")
	//Nhận vào một tham số tùy chọn kw là từ khóa (Optional<String> kw).
	public List<Brand> searchBrand(@RequestParam("kw") Optional<String> kw){
		String keyword = kw.orElse(null); // sử dụng orElse của Optional để lấy từ khóa hoặc null nếu không có giá trị được cung cấp.
		if(keyword != null) { 
		//Nếu keyword khác null, thực hiện tìm kiếm Brand bằng cách gọi phương thức findByName của bService 
			//với mẫu tìm kiếm là "%"+keyword+"%" để tìm các Brand có tên chứa từ khóa.
			return bService.findByName("%"+keyword+"%");
		}else {
			//Nếu keyword là null, trả về tất cả các Brand bằng cách gọi phương thức getAllBrand
			return this.getAllBrand();
		}
	}
	
	@PostMapping("")
	//Nhận vào một đối tượng Brand được gửi lên trong phần thân của request (@RequestBody Brand brand).
	public ResponseEntity<Brand> postBrand(@RequestBody Brand brand){
		if(bService.existsById(brand.getId())) { // ktra brand có tồn tại trong cơ sở dữ liệu hay kh nếu brand tồn tại return về 
			return ResponseEntity.badRequest().build(); 
		}else {
			return ResponseEntity.ok(bService.save(brand));
		}
	}
	
	@PutMapping("/{id}")
	//Nhận vào id của Brand cần cập nhật thông qua @PathVariable("id").
	public ResponseEntity<Brand> putBrand(@PathVariable("id") String id, @RequestBody Brand brand){
		// ktra brand có tồn tại trong db hay kh, nếu kh trả về ResponseEntity.notFound().build();
		if(!bService.existsById(id)) {
			return ResponseEntity.notFound().build();
		}else {
			return ResponseEntity.ok(bService.save(brand));
		}
	}
	// chỉ ra kết quả của yêu cầu xóa có thể là mã HTTP not found nếu Brand không tồn tại hoặc mã HTTP ok nếu xóa thành công.
	@DeleteMapping("/{id}")
	// nhận id của brand cần xóa thông qua @PathVariavle("id")
	public ResponseEntity<Void> deleteBrand(@PathVariable("id") String id){
		if(!bService.existsById(id)) { // ktra brand có tồn tại trong db hay kh
			return ResponseEntity.notFound().build(); // => kh tồn tại
		}else {
			// nếu brand tồn tại => thực hiện xóa brand
			Brand brand = bService.findById(id);
			//Kiểm tra Brand có  tệp hình ảnh không phải là "logo.jpg" hay không. Nếu có, gọi phương thức delete của uService để xóa tệp hình ảnh đó.
			String filename = brand.getImage(); 
			System.out.println(filename);
			if(!filename.equalsIgnoreCase("logo.jpg")) {
				uService.delete("brand", filename);
			}
			bService.deleteById(id);
			return ResponseEntity.ok().build(); 	
		}
	}
	
}

