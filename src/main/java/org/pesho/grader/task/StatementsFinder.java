//package org.pesho.grader.task;
//
//import java.io.File;
//import java.util.Arrays;
//
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.text.PDFTextStripper;
//import org.apache.tika.Tika;
//
//public class StatementsFinder {
//	
//	public static void main(String[] args) throws Exception {
////        File myFile = new File("C:\\Users\\Petar\\Documents\\informatics\\noi2_2021\\A\\colors\\statement\\colors_statement.pdf");
//        File myFile = new File("C:\\Users\\Petar\\Documents\\informatics\\noi3\\2021\\A\\task\\statement\\sets_statement.pdf");
////        File myFile = new File("C:\\Users\\Petar\\Documents\\informatics\\noi3\\2021\\A\\task\\statement\\moscow.pdf");
//
//        try (PDDocument doc = PDDocument.load(myFile)) {
//            PDFTextStripper stripper = new PDFTextStripper();
//            String text = stripper.getText(doc);
//
//            System.out.println("Text size: " + text.length() + " characters:");
//            System.out.println(text);
//        }
//        
//        Tika tika = new Tika();
////        Metadata metadata = new Metadata();
////        TikaInputStream reader = TikaInputStream.get(new BufferedInputStream(new FileInputStream(myFile)));
////        String contents = tika.parseToString(reader, metadata);
////        String content = new Tika().parseToString(myFile);
////        System.out.println(contents);
//        
////        Parser parser = new AutoDetectParser();
////        ContentHandler handler = new BodyContentHandler();
////        Metadata metadata = new Metadata();
////        ParseContext context = new ParseContext();
//
////        parser.parse(new BufferedInputStream(new FileInputStream(myFile)), handler, metadata, context);
////        System.out.println(handler.toString());
//        
////        Detector detector = new DefaultDetector();
////        Metadata metadata = new Metadata();
////     
////        MediaType mediaType = detector.detect(new BufferedInputStream(new FileInputStream(myFile)), metadata);
////        System.out.println(mediaType.toString());
//		
//        
//        String type = tika.detect(myFile);
//        System.out.println(type);
//        String filecontent = tika.parseToString(myFile);
//        System.out.println("Extracted Content: " + filecontent);
//        Arrays.stream(filecontent.split("\n")).forEach(line -> {
//        	System.out.println(line);
//        	for (byte b: line.getBytes()) {
//        		System.out.print(b + " ");
//        	}
//        	System.out.println();
//        });
//        
////        //parse method parameters
////        Parser parser = new AutoDetectParser();
////        BodyContentHandler handler = new BodyContentHandler();
////        Metadata metadata = new Metadata();
////        FileInputStream inputstream = new FileInputStream(myFile);
////        ParseContext context = new ParseContext();
////        
////        //parsing the file
////        parser.parse(inputstream, handler, metadata, context);
////        System.out.println("File content : " + handler.toString());
//        
////        Parser parser = new AutoDetectParser();
////        BodyContentHandler handler = new BodyContentHandler();
////        Metadata metadata = new Metadata();
////        FileInputStream content = new FileInputStream(myFile);
////
////        //Parsing the given document
////        parser.parse(content, handler, metadata, new ParseContext());
////
////        LanguageIdentifier object = new LanguageIdentifier(handler.toString());
////        System.out.println("Language name :" + object.getLanguage());
//        
//        
////        BodyContentHandler handler = new BodyContentHandler();
////        Metadata metadata = new Metadata();
////        FileInputStream inputstream = new FileInputStream(myFile);
////        ParseContext pcontext = new ParseContext();
////        
////        //parsing the document using PDF parser
////        PDFParser pdfparser = new PDFParser(); 
////        pdfparser.parse(inputstream, handler, metadata,pcontext);
////        System.out.println(handler.toString());
////        //getting the content of the document
//	}
//
//}
