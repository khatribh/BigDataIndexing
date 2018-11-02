package com.example.demo;

import com.example.utils.StringUtils;
import com.example.utils.ValdiationUtils;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class BigDataController {
	
	
	private static JedisPool pool = null;
	
	@RequestMapping("/")
	public String index() {
		return "Welcome to Big Data Indexing Spring Boot Application!";
	}
	@RequestMapping(value="/plan/schema",method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity storeSchema(@RequestHeader HttpHeaders headers, @RequestBody String entity){

		Jedis jedis = new Jedis(StringUtils.LOCALHOST, StringUtils.REDIS_PORT);
		
		if(entity==null) {
			return new ResponseEntity("No Schema Received!", HttpStatus.BAD_REQUEST);
		}
		jedis.set("Plan_schema", entity );
		//this.finalSchemeFile = entity;
		return new ResponseEntity("Schema is posted successfully!",HttpStatus.CREATED);
	}
	@RequestMapping(value="/plan",method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity createPlan(@RequestHeader HttpHeaders headers, @RequestBody String entity) throws ProcessingException, IOException{

		Jedis jedis = new Jedis(StringUtils.LOCALHOST, StringUtils.REDIS_PORT);
//		//File finalSchemeFile = new File("C://Users//Bhumika//Documents//BigDataIndex//SchemaFile.txt");
//		String path = "C:Users/Bhumika/Documents/BigDataIndex/SchemaFile.txt";
		String schemeFile = jedis.get("Plan_schema");
		System.out.println(schemeFile);
		if(entity==null) {
			return new ResponseEntity("No Schema Received!", HttpStatus.BAD_REQUEST);
		}
		if (ValdiationUtils.isJsonValid(schemeFile, entity)){
			UUID uuid = UUID.randomUUID();
			String id = "plan-"+uuid;
			jedis.set(id, entity);
	    	return new ResponseEntity("Data posted successfully and the key is "+id, HttpStatus.CREATED);
	    }else{
	    	return new ResponseEntity("Data Schema is not Valid!", HttpStatus.BAD_REQUEST);
	    }
		
	}
	@RequestMapping(value="/plan/{id}",method = RequestMethod.GET)
	public ResponseEntity createPlan(@PathVariable String id) throws ProcessingException, IOException{

		
		pool = new JedisPool(StringUtils.REDIS_HOST, StringUtils.REDIS_PORT);
		Jedis jedis = pool.getResource();
		String resultPlan = jedis.get(id);
		
		if(resultPlan == null || resultPlan.isEmpty()) {
			return new ResponseEntity("No Data Found", HttpStatus.NOT_FOUND);
		}
		else{
			return new ResponseEntity(resultPlan, HttpStatus.ACCEPTED);
		}
		
	}
	@RequestMapping(value="/plan/{id}",method = RequestMethod.DELETE)
	public ResponseEntity deletePlan(@PathVariable String id) throws ProcessingException, IOException{

		
		pool = new JedisPool(StringUtils.REDIS_HOST, StringUtils.REDIS_PORT);
		Jedis jedis = pool.getResource();
		Long resultPlan = jedis.del(id);
		
		if(resultPlan == null) {
			return new ResponseEntity("No Data Found to be Deleted", HttpStatus.NOT_FOUND);
		}
		else{
			
			return new ResponseEntity("Deleted Successfully", HttpStatus.ACCEPTED);
		}
		
	}
	@RequestMapping(value="/plan/{id}",method = RequestMethod.PUT)
	public ResponseEntity updatePlan(@PathVariable String id, @RequestBody String entity) throws ProcessingException, IOException{

		
		pool = new JedisPool(StringUtils.REDIS_HOST, StringUtils.REDIS_PORT);
		Jedis jedis = pool.getResource();
		String resultPlan = jedis.get(id);
		System.out.println(resultPlan);
		String newid = id;
		
		if(resultPlan == null && resultPlan.isEmpty()) {
			return new ResponseEntity("No Data Found to be Updated", HttpStatus.NOT_FOUND);
		}
		else{
			
			System.out.println(entity);
			String schemaFile = jedis.get("Plan_schema");
			if (ValdiationUtils.isJsonValid(schemaFile, entity)){
				jedis.del(newid);
				jedis.set(newid, entity);
		    	return new ResponseEntity("Updated Sucessfully", HttpStatus.CREATED);
		    }else{
		    	return new ResponseEntity("Data Schema is not Valid!", HttpStatus.BAD_REQUEST);
		    }
			
		}
		
	}
	
//	public String readFile(String file) throws IOException {
//	    BufferedReader reader = new BufferedReader(new FileReader(file));
//	    System.out.println(reader);
//	    String   line = null;
//	    StringBuilder  stringBuilder = new StringBuilder();
//	   
//
//	    try {
//	        while((line = reader.readLine()) != null) {
//	        	System.out.println(line);
//	            stringBuilder.append(line);
//	           
//	        }
//	        return stringBuilder.toString();
//	    } finally {
//	        reader.close();
//	    }
//	}
//	public static String readLineByLineJava8(String filePath)
//    {
//		System.out.println(filePath);
//        StringBuilder contentBuilder = new StringBuilder();
// 
//        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
//        {
//            stream.forEach(s -> contentBuilder.append(s));
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
// 
//        return contentBuilder.toString();
//    }

	

}
