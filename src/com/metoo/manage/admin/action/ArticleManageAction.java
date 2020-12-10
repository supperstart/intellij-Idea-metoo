package com.metoo.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.beans.BeanUtils;
import com.metoo.core.beans.BeanWrapper;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Article;
import com.metoo.foundation.domain.ArticleClass;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsFloor;
import com.metoo.foundation.domain.query.ArticleQueryObject;
import com.metoo.foundation.domain.query.GoodsFloorQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IArticleClassService;
import com.metoo.foundation.service.IArticleService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsFloorService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;

/**
 * 
* <p>Title: ArticleManageAction.java</p>

* <p>Description:系统文章管理控制器，用来发布、修改系统文章信息 </p>

* <p>Copyright: Copyright (c) 2014</p>

* <p>Company: 湖南觅通科技有限公司</p>

* @author erikzhang

* @date 2014-5-21

* @version koala_b2b2c v2.0 2015版 
 */
@Controller
public class ArticleManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IArticleService articleService;
	@Autowired
	private IArticleClassService articleClassService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IAccessoryService accessoryService; 
	@Autowired
	private IGoodsFloorService goodsfloorService;
	
	/**
	 * Article列表页
	 * 
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @param request
	 * @return
	 */
	@SecurityMapping(title = "文章列表", value = "/admin/article_list.htm*", rtype = "admin", rname = "文章管理", rcode = "article", rgroup = "网站")
	@RequestMapping("/admin/article_list.htm")
	public ModelAndView article_list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType) {
		ModelAndView mv = new JModelAndView("admin/blue/article_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		String url = this.configService.getSysConfig().getAddress();//http://ebuyair.metoo-souq.com/
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String params = "";
		//分页
		ArticleQueryObject qo = new ArticleQueryObject(currentPage, mv,
				orderBy, orderType);
		
		//封装表单数据到实体
		WebForm wf = new WebForm();
		wf.toQueryPo(request, qo, Article.class, mv);//qo Article 为查询对象  mv为返回视图
		
		IPageList pList = this.articleService.list(qo);
		
		
		//System.out.println(pList);
		
		CommUtil.saveIPageList2ModelAndView(url + "/admin/article_list.htm",
				"", params, pList, mv);
		return mv;
	}
	
	
	
	/**
	 * article添加管理
	 * 
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@SecurityMapping(title = "文章添加", value = "/admin/article_add.htm*", rtype = "admin", rname = "文章管理", rcode = "article", rgroup = "网站")
	@RequestMapping("/admin/article_add.htm")
	public ModelAndView article_add(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String class_id) {
		ModelAndView mv = new JModelAndView("admin/blue/article_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		List<ArticleClass> acs = this.articleClassService
				.query("select obj from ArticleClass obj where obj.parent.id is null order by obj.sequence asc",
						null, -1, -1);
		Article obj = new Article();
		obj.setDisplay(true);
		if (class_id != null && !class_id.equals(""))
			obj.setArticleClass(this.articleClassService.getObjById(Long
					.parseLong(class_id)));
		List<GoodsClass> goodsclass = this.goodsClassService.query(
				"select obj from GoodsClass obj where obj.parent.id is null", null,
				-1, -1);
		mv.addObject("goodsclass",goodsclass);
		mv.addObject("obj", obj);
		mv.addObject("acs", acs);
		mv.addObject("currentPage", currentPage);
		return mv;
	}

	/**
	 * article编辑管理
	 * 
	 * @param id
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@SecurityMapping(title = "文章编辑", value = "/admin/article_edit.htm*", rtype = "admin", rname = "文章管理", rcode = "article", rgroup = "网站")
	@RequestMapping("/admin/article_edit.htm")
	public ModelAndView article_edit(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView("admin/blue/article_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if (id != null && !id.equals("")) {
			Article article = this.articleService
					.getObjById(Long.parseLong(id));
			List<ArticleClass> acs = this.articleClassService
					.query("select obj from ArticleClass obj where obj.parent.id is null order by obj.sequence asc",
							null, -1, -1);
			List<GoodsClass> goodsclass = this.goodsClassService.query(
					"select obj from GoodsClass obj where obj.parent.id is null", null,
					-1, -1);
			GoodsFloorQueryObject qo = new GoodsFloorQueryObject(currentPage, mv,
					"gf_sequence", "asc");
			qo.addQuery("obj.gf_level", new SysMap("gf_level", 0), "=");
			IPageList pList = this.goodsfloorService.list(qo);
			List<GoodsFloor> goodsFloors = pList.getResult();
			mv.addObject("goodsclass",goodsclass);
			mv.addObject("goodsfloor",goodsFloors);
			mv.addObject("acs", acs);
			mv.addObject("obj", article);
			mv.addObject("currentPage", currentPage);
			mv.addObject("edit", true);
		}
		return mv;
	}

	/**
	 * article保存管理
	 * 
	 * @param id
	 * @return
	 */
	@SecurityMapping(title = "文章保存", value = "/admin/article_save.htm*", rtype = "admin", rname = "文章管理", rcode = "article", rgroup = "网站")
	@RequestMapping("/admin/article_save.htm")
	public ModelAndView article_save(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage,
			String cmd, String list_url, String add_url, String class_id,
			String content, String floor_id) {
		WebForm wf = new WebForm();
		Article article = null;
		if (id.equals("")) {
			article = wf.toPo(request, Article.class);
			article.setAddTime(new Date());
		} else {
			Article obj = this.articleService.getObjById(Long.parseLong(id));
			article = (Article) wf.toPo(request, obj);
			article.setGoodsFloor(null);
		}
		article.setArticleClass(this.articleClassService.getObjById(Long
				.parseLong(class_id)));
		if(!floor_id.equals("")){
			article.setGoodsFloor(this.goodsfloorService.getObjById(Long
					.parseLong(floor_id)));
		}
		String uploadFilePath = this.configService.getSysConfig().getUploadFilePath();
		String saveFilePathName = uploadFilePath+"/"+"article";
		Map map = new HashMap();
		String fileName = "";
		if(article.getArticle_acc() != null){
			fileName = article.getArticle_acc().getName();
		}
		try {
			map = CommUtil.httpsaveFileToServer(request, "acc", saveFilePathName, fileName, null);
			Accessory acc = null;
			
			if (fileName.equals("")) {
				if (map.get("fileName") != "") {
					acc = new Accessory();
					acc.setName(CommUtil.null2String(map.get("fileName")));
					acc.setExt(CommUtil.null2String(map.get("mime")));
					acc.setSize(BigDecimal.valueOf(CommUtil.null2Double(map
							.get("fileSize"))));
					acc.setPath(uploadFilePath + "/article");
					acc.setWidth(CommUtil.null2Int(map.get("width")));
					acc.setHeight(CommUtil.null2Int(map.get("height")));
					acc.setAddTime(new Date());
					this.accessoryService.save(acc);
					article.setArticle_acc(acc);
				}
			} else {
				if (map.get("fileName") != "") {
					acc = article.getArticle_acc();
					acc.setName(CommUtil.null2String(map.get("fileName")));
					acc.setExt(CommUtil.null2String(map.get("mime")));
					acc.setSize(BigDecimal.valueOf(CommUtil.null2Double(map
							.get("fileSize"))));
					acc.setPath(uploadFilePath + "/article");
					acc.setWidth(CommUtil.null2Int(map.get("width")));
					acc.setHeight(CommUtil.null2Int(map.get("height")));
					acc.setAddTime(new Date());
					this.accessoryService.update(acc);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (id.equals("")) {
			this.articleService.save(article);
		} else
			this.articleService.update(article);
		ModelAndView mv = new JModelAndView("admin/blue/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("list_url", list_url);
		mv.addObject("op_title", "保存文章成功");
		if (add_url != null) {
			mv.addObject("add_url", add_url + "?currentPage=" + currentPage
					+ "&class_id=" + class_id);
		}
		return mv;
	}

	@SecurityMapping(title = "文章删除", value = "/admin/article_del.htm*", rtype = "admin", rname = "文章管理", rcode = "article", rgroup = "网站")
	@RequestMapping("/admin/article_del.htm")
	public String article_del(HttpServletRequest request, String mulitId) {
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				Article article = this.articleService.getObjById(Long
						.parseLong(id));
				this.articleService.delete(Long.parseLong(id));
			}
		}
		return "redirect:article_list.htm";
	}

	@SecurityMapping(title = "文章AJAX更新", value = "/admin/article_ajax.htm*", rtype = "admin", rname = "文章管理", rcode = "article", rgroup = "网站")
	@RequestMapping("/admin/article_ajax.htm")
	public void article_ajax(HttpServletRequest request, HttpServletResponse response,
			String id, String fieldName, String value)
			throws ClassNotFoundException {
		Article obj = this.articleService.getObjById(Long.parseLong(id));
		Field[] fields = Article.class.getDeclaredFields();
		BeanWrapper wrapper = new BeanWrapper(obj);
		Object val = null;
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				Class clz = Class.forName("java.lang.String");
				if (field.getType().getName().equals("int")) {
					clz = Class.forName("java.lang.Integer");
				}
				if (field.getType().getName().equals("boolean")) {
					clz = Class.forName("java.lang.Boolean");
				}
				if (!value.equals("")) {
					val = BeanUtils.convertType(value, clz);
				} else {
					val = !CommUtil.null2Boolean(wrapper
							.getPropertyValue(fieldName));
				}
				wrapper.setPropertyValue(fieldName, val);
			}
		}
		this.articleService.update(obj);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(val.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@RequestMapping("/admin/article_mark.htm")
	public void article_mark(HttpServletRequest request,
			HttpServletResponse response, String mark, String id) {
		Map params = new HashMap();
		params.put("mark", mark.trim());
		params.put("id", CommUtil.null2Long(id));
		List<Article> arts = this.articleService
				.query("select obj from Article obj where obj.mark=:mark and obj.id!=:id",
						params, -1, -1);
		boolean ret = true;
		if (arts.size() > 0) {
			ret = true;
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}