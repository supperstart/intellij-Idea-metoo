package com.metoo.lucene.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.metoo.lucene.parse.ShopQueryParser;

/** */
/**
 * author lighter date 2006-8-7
 */
public class TextFileIndexer {

	public static void main(String[] args) throws Exception {
		//CreateIndex();
		Search();
	}

	public static void Search() throws Exception {
		File indexDir = new File("D:\\test\\index");
		IndexReader reader = null;
		reader = IndexReader.open(FSDirectory.open(indexDir));

		IndexSearcher isearcher = new IndexSearcher(reader);

		Analyzer analyzer = new IKAnalyzer();
		QueryParser parser = new ShopQueryParser(Version.LUCENE_4_10_0, "body",
				analyzer);

		parser.setAllowLeadingWildcard(false);
		Query query = parser.parse("1.txt");

		TopDocs topDocs = null;

		topDocs = isearcher.search(query, 100);
		
		System.out.println("======="+topDocs.totalHits);
		
		for (int i = 0; i < topDocs.totalHits; i++) {
			Document doc = isearcher.doc(topDocs.scoreDocs[i].doc);

			String title = doc.get("body");
			System.out.println("========"+title);
		}
	}

	public static void CreateIndex() throws Exception {
		/**//* 指明要索引文件夹的位置,这里是C盘的S文件夹下 */
		File fileDir = new File("D:\\test\\s");

		File[] textFiles = fileDir.listFiles();
		long startTime = new Date().getTime();

		// 增加document到索引去
		for (int i = 0; i < textFiles.length; i++) {
			if (textFiles[i].isFile()
					&& textFiles[i].getName().endsWith(".txt")) {
				System.out.println(" File  " + textFiles[i].getCanonicalPath()
						+ " 正在被索引. ");
				String temp = FileReaderAll(textFiles[i].getCanonicalPath(),
						"GBK");
				System.out.println(temp);
				Document document = new Document();
				Field FieldTitle = new Field("title", textFiles[i].getName(),
						Field.Store.YES, Field.Index.ANALYZED);
				Field FieldPath = new Field("path", textFiles[i].getPath(),
						Field.Store.YES, Field.Index.NO);
				Field FieldBody = new Field("body", temp, Field.Store.YES,
						Field.Index.ANALYZED,
						Field.TermVector.WITH_POSITIONS_OFFSETS);
				document.add(FieldPath);
				document.add(FieldBody);

				// indexWriter.addDocument(document);
				writeDoc(document);
			}

		}
		
		// 测试一下索引的时间
		long endTime = new Date().getTime();
		System.out.println(" 这花费了" + (endTime - startTime)
				+ "  毫秒来把文档增加到索引里面去! " + fileDir.getPath());
	}

	public static String FileReaderAll(String FileName, String charset)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(FileName), charset));
		String line = new String();
		String temp = new String();

		while ((line = reader.readLine()) != null) {
			temp += line;
		}
		reader.close();
		return temp;
	}

	public static void writeDoc(Document document) throws IOException {

		/**//* 这里放索引文件的位置 */
		File indexDir = new File("D:\\test\\index");
		// Analyzer luceneAnalyzer = new StandardAnalyzer();
		IndexWriterConfig iwconfig = new IndexWriterConfig(
				Version.LUCENE_4_10_0, new IKAnalyzer());
		IndexWriter indexWriter = new IndexWriter(FSDirectory.open(indexDir),
				iwconfig);

		try {

			if (indexWriter.isLocked(indexWriter.getDirectory())) {
				indexWriter.unlock(indexWriter.getDirectory());
			}
			// Document document = builderDocument(vo);
			indexWriter.addDocument(document);
			indexWriter.commit();
			indexWriter.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	//删除所有索引
	/**
	 * 删除指定索引库下面的所有 索引数据
	 * @param indexDir
	 */
	/*public static void indexDelAll(File indexDir) throws IOException {
	    if (indexDir == null || !indexDir.exists() || indexDir.isFile()) {
	        return;
	    }
	    // 创建 IKAnalyzer 中文分词器
	    Analyzer analyzer = new IKAnalyzer();
	    Directory directory = FSDirectory.open(indexDir.toPath());
	    // 创建 索引写配置对象，传入分词器
	    IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    // 创建 索引写对象
	    IndexWriter indexWriter = new IndexWriter(directory, config);
	    *//** 删除所有索引
	     * 如果索引库中的索引已经被删除，则重复删除时无效*//*
	    indexWriter.deleteAll();
	    *//** 虽然不 commit，也会生效，但建议做提交操作，*//*
	    indexWriter.commit();
	    *//**  关闭流，里面会自动 flush*//*
	    indexWriter.close();
	}*/
}
