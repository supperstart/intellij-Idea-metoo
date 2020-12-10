package com.metoo.manage.admin.action;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Album;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.WaterMark;
import com.metoo.foundation.domain.query.AccessoryQueryObject;
import com.metoo.foundation.domain.query.AlbumQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IAlbumService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.foundation.service.IWaterMarkService;
import com.metoo.manage.admin.tools.ImageTools;
import com.metoo.view.web.tools.AlbumViewTools;
import com.metoo.view.web.tools.StoreViewTools;

/**
 * 
 * <p>
 * Title: AlbumSelfManageAction.java
 * </p>
 * 
 * <p>
 * Description:自营相册管理类
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.koala.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014年5月27日
 * 
 * @version koala_b2b2c 2.0
 */
@Controller
public class SelfAlbumManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IWaterMarkService waterMarkService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsSerivce;
	@Autowired
	private AlbumViewTools albumViewTools;
	@Autowired
	private DatabaseTools databaseTools;
	@Autowired
	private IWaterMarkService watermarkService;
	@Autowired
	private StoreViewTools storeViewTools;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ImageTools ImageTools;

	@SecurityMapping(title = "相册列表", value = "/admin/album.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album.htm")
	public ModelAndView album(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView("admin/blue/album.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		AlbumQueryObject aqo = new AlbumQueryObject();
		aqo.addQuery("obj.user.userRole", new SysMap("user_userRole", "ADMIN"),
				"=");
		aqo.setCurrentPage(CommUtil.null2Int(currentPage));
		aqo.setOrderBy("album_sequence");
		aqo.setOrderType("asc");
		aqo.setPageSize(30);
		IPageList pList = this.albumService.list(aqo);
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		CommUtil.saveIPageList2ModelAndView(url + "/admin/album.htm", "", "",
				pList, mv);
		mv.addObject("albumViewTools", albumViewTools);
		return mv;
	}

	@SecurityMapping(title = "修改相册", value = "/admin/album_add.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_add.htm")
	public ModelAndView album_add(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView("admin/blue/album_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("currentPage", currentPage);
		return mv;
	}

	@SecurityMapping(title = "新增相册", value = "/admin/album_edit.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_edit.htm")
	public ModelAndView album_edit(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String id) {
		ModelAndView mv = new JModelAndView("admin/blue/album_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Album obj = this.albumService.getObjById(CommUtil.null2Long(id));
		mv.addObject("obj", obj);
		mv.addObject("currentPage", currentPage);
		return mv;
	}

	@SecurityMapping(title = "相册保存", value = "/admin/album_save.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_save.htm")
	public ModelAndView album_save(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		WebForm wf = new WebForm();
		Album album = null;
		if (id.equals("")) {
			album = wf.toPo(request, Album.class);
			album.setAddTime(new Date());
		} else {
			Album obj = this.albumService.getObjById(Long.parseLong(id));
			album = (Album) wf.toPo(request, obj);
		}
		album.setUser(this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId()));
		boolean ret = true;
		if (id.equals("")) {
			ret = this.albumService.save(album);
		} else
			ret = this.albumService.update(album);
		ModelAndView mv = new JModelAndView("admin/blue/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("list_url", CommUtil.getURL(request) + "/admin/album.htm"
				+ "?currentPage=" + currentPage);
		mv.addObject("op_title", "保存相册成功");
		return mv;
	}

	@SecurityMapping(title = "图片上传", value = "/admin/album_upload.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_upload.htm")
	public ModelAndView album_upload(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String album_id) {
		ModelAndView mv = new JModelAndView("admin/blue/album_upload.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Map params = new HashMap();
		params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
		List<Album> objs = this.albumService
				.query("select obj from Album obj where obj.user.id=:user_id order by obj.album_sequence asc",
						params, -1, -1);
		mv.addObject("objs", objs);
		mv.addObject("currentPage", currentPage);
		mv.addObject("album_id", album_id);
		mv.addObject("jsessionid", request.getSession().getId());
		mv.addObject("imageSuffix", this.storeViewTools
				.genericImageSuffix(this.configService.getSysConfig()
						.getImageSuffix()));
		// 生成user_id字符串，防止在特定环境下swf上传无法获取session
		String temp_begin = request.getSession().getId().toString()
				.substring(0, 5);
		String temp_end = CommUtil.randomInt(5);
		String user_id = CommUtil.null2String(SecurityUserHolder
				.getCurrentUser().getId());
		mv.addObject("session_u_id", temp_begin + user_id + temp_end);
		return mv;
	}

	@SecurityMapping(title = "相册删除", value = "/admin/album_del.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_del.htm")
	public String album_del(HttpServletRequest request, String mulitId) {
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				Album album = this.albumService.getObjById(CommUtil
						.null2Long(id));
				if (album != null) {
					Map params = new HashMap();
					params.put("album_id", album.getId());
					List<Accessory> accs = this.accessoryService
							.query("select obj from Accessory obj where obj.album.id=:album_id",
									params, -1, -1);
					for (Accessory acc : accs) {
						CommUtil.del_acc(request, acc);
						for (Goods goods : acc.getGoods_main_list()) {
							goods.setGoods_main_photo(null);
							this.goodsService.update(goods);
						}
						for (Goods goods1 : acc.getGoods_list()) {
							goods1.getGoods_photos().remove(acc);
							this.goodsService.update(goods1);
						}
						this.accessoryService.delete(acc.getId());
					}
					this.albumService.delete(Long.parseLong(id));
				}
			}
		}
		return "redirect:album.htm";
	}

	@SecurityMapping(title = "相册封面设置", value = "/admin/album_cover.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_cover.htm")
	public String album_cover(HttpServletRequest request, String album_id,
			String id, String currentPage) {
		Accessory album_cover = this.accessoryService.getObjById(Long
				.parseLong(id));
		Album album = this.albumService.getObjById(Long.parseLong(album_id));
		album.setAlbum_cover(album_cover);
		this.albumService.update(album);
		return "redirect:album_image.htm?id=" + album_id + "&currentPage="
				+ currentPage;
	}

	@SecurityMapping(title = "相册转移", value = "/admin/album_transfer.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_transfer.htm")
	public ModelAndView album_transfer(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String album_id,
			String id) {
		ModelAndView mv = new JModelAndView("admin/blue/album_transfer.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Map params = new HashMap();
		params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
		List<Album> objs = this.albumService
				.query("select obj from Album obj where obj.user.id=:user_id order by obj.album_sequence asc",
						params, -1, -1);
		mv.addObject("objs", objs);
		mv.addObject("currentPage", currentPage);
		mv.addObject("album_id", album_id);
		mv.addObject("mulitId", id);
		return mv;
	}

	@SecurityMapping(title = "图片转移相册", value = "/admin/album_transfer_save.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_transfer_save.htm")
	public String album_transfer_save(HttpServletRequest request,
			String mulitId, String album_id, String to_album_id,
			String currentPage) {
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				Accessory acc = this.accessoryService.getObjById(Long
						.parseLong(id));
				Album to_album = this.albumService.getObjById(Long
						.parseLong(to_album_id));
				acc.setAlbum(to_album);
				this.accessoryService.update(acc);
			}
		}
		return "redirect:album_image.htm?id=" + album_id + "&currentPage="
				+ currentPage;
	}

	@SecurityMapping(title = "图片列表", value = "/admin/album_image.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_image.htm")
	public ModelAndView album_image(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView("admin/blue/album_image.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Album album = this.albumService.getObjById(Long.parseLong(id));
		AccessoryQueryObject aqo = new AccessoryQueryObject();
		if (id != null && !id.equals("")) {
			aqo.addQuery("obj.album.id",
					new SysMap("album_id", CommUtil.null2Long(id)), "=");
		} else {
			aqo.addQuery("obj.album.id is null", null);
		}
		aqo.setCurrentPage(CommUtil.null2Int(currentPage));
		aqo.setPageSize(15);
		aqo.setOrderBy("addTime");
		aqo.setOrderType("desc");
		IPageList pList = this.accessoryService.list(aqo);
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		CommUtil.saveIPageList2ModelAndView(url + "/admin/album_image.htm", "",
				"&id=" + id, pList, mv);
		Map params = new HashMap();
		params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
		List<Album> albums = this.albumService
				.query("select obj from Album obj where obj.user.id=:user_id order by obj.album_sequence asc",
						params, -1, -1);
		mv.addObject("albums", albums);
		mv.addObject("album", album);
		return mv;
	}

	@SecurityMapping(title = "图片幻灯查看", value = "/admin/image_slide.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/image_slide.htm")
	public ModelAndView image_slide(HttpServletRequest request,
			HttpServletResponse response, String album_id, String id) {
		ModelAndView mv = new JModelAndView("admin/blue/image_slide.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Album album = this.albumService
				.getObjById(CommUtil.null2Long(album_id));
		mv.addObject("album", album);
		Accessory current_img = this.accessoryService.getObjById(CommUtil
				.null2Long(id));
		mv.addObject("current_img", current_img);
		mv.addObject("ImageTools", ImageTools);
		return mv;
	}

	@SecurityMapping(title = "相册内图片删除", value = "/admin/album_img_del.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_img_del.htm")
	public String album_img_del(HttpServletRequest request, String mulitId,
			String album_id, String currentPage) {
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				Accessory acc = this.accessoryService.getObjById(Long
						.parseLong(id));
				String middle_path = request.getSession().getServletContext()
						.getRealPath("/")
						+ acc.getPath()
						+ File.separator
						+ acc.getName()
						+ "_middle." + acc.getExt();
				CommUtil.deleteFile(middle_path);
				CommUtil.del_acc(request, acc);
				for (Goods goods : acc.getGoods_main_list()) {
					goods.setGoods_main_photo(null);
					this.goodsSerivce.update(goods);
				}
				for (Goods goods : acc.getGoods_list()) {
					goods.getGoods_photos().remove(acc);
					this.goodsSerivce.update(goods);
				}
				// 这个acc附件是否是这个相册的封面，如果是请删除
				Map params = new HashMap();
				params.put("ac_id", acc.getId());
				List<Album> albums = this.albumService
						.query("select obj from Album obj where obj.album_cover.id=:ac_id",
								params, 0, 1);
				if (albums.size() > 0) {
					Album album = albums.get(0);
					album.setAlbum_cover(null);
					this.albumService.update(album);
				}
				this.accessoryService.delete(acc.getId());
			}
		}
		return "redirect:album_image.htm?id=" + album_id + "&currentPage="
				+ currentPage;
	}

	@SecurityMapping(title = "图片转移相册", value = "/admin/album_watermark.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/album_watermark.htm")
	public String album_watermark(HttpServletRequest request, String mulitId,
			String album_id, String to_album_id, String currentPage) {
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		WaterMark waterMark = this.waterMarkService.getObjByProperty(null,
				"user.id", user.getId());
		if (waterMark != null) {
			String[] ids = mulitId.split(",");
			for (String id : ids) {
				if (!id.equals("")) {
					Accessory acc = this.accessoryService.getObjById(Long
							.parseLong(id));
					String path = request.getSession().getServletContext()
							.getRealPath("/")
							+ acc.getPath() + File.separator + acc.getName();
					path = path.replace("/", "\\");
					if (waterMark.isWm_image_open()) {
						String wm_path = request.getSession()
								.getServletContext().getRealPath("/")
								+ waterMark.getWm_image().getPath()
								+ File.separator
								+ waterMark.getWm_image().getName();
						CommUtil.waterMarkWithImage(wm_path, path,
								waterMark.getWm_image_pos(),
								waterMark.getWm_image_alpha());
					}
					if (waterMark.isWm_text_open()) {
						Font font = new Font(waterMark.getWm_text_font(),
								Font.BOLD, waterMark.getWm_text_font_size());
						CommUtil.waterMarkWithText(path, path,
								waterMark.getWm_text(),
								waterMark.getWm_text_color(), font,
								waterMark.getWm_text_pos(), 100f);
					}
				}
			}
		}
		return "redirect:album_image.htm?id=" + album_id + "&currentPage="
				+ currentPage;
	}

	@SecurityMapping(title = "图片水印", value = "/admin/watermark.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/watermark.htm")
	public ModelAndView watermark(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("admin/blue/watermark.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Map params = new HashMap();
		params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
		List<WaterMark> wms = this.watermarkService.query(
				"select obj from WaterMark obj where obj.user.id=:user_id",
				params, -1, -1);
		if (wms.size() > 0) {
			mv.addObject("obj", wms.get(0));
		}
		return mv;
	}

	/**
	 * watermark保存管理
	 * 
	 * @param id
	 * @return
	 */
	@SecurityMapping(title = "图片水印保存", value = "/admin/watermark_save.htm*", rtype = "admin", rname = "自营相册", rcode = "album_admin", rgroup = "自营")
	@RequestMapping("/admin/watermark_save.htm")
	public ModelAndView watermark_save(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = null;
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		if (SecurityUserHolder.getCurrentUser() != null) {
			WebForm wf = new WebForm();
			WaterMark watermark = null;
			if (id.equals("")) {
				watermark = wf.toPo(request, WaterMark.class);
				watermark.setAddTime(new Date());
			} else {
				WaterMark obj = this.watermarkService.getObjById(Long
						.parseLong(id));
				watermark = (WaterMark) wf.toPo(request, obj);
			}
			watermark.setUser(user);
			String path = request.getSession().getServletContext()
					.getRealPath("/")
					+ "upload/wm";
			try {
				Map map = CommUtil.saveFileToServer(request, "wm_img", path,
						null, null);
				if (!map.get("fileName").equals("")) {
					Accessory wm_image = new Accessory();
					wm_image.setAddTime(new Date());
					wm_image.setHeight(CommUtil.null2Int(map.get("height")));
					wm_image.setName(CommUtil.null2String(map.get("fileName")));
					wm_image.setPath("upload/wm");
					wm_image.setSize(BigDecimal.valueOf(CommUtil
							.null2Double(map.get("fileSize"))));
					wm_image.setUser(SecurityUserHolder.getCurrentUser());
					wm_image.setWidth(CommUtil.null2Int("width"));
					this.accessoryService.save(wm_image);
					watermark.setWm_image(wm_image);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (id.equals("")) {
				this.watermarkService.save(watermark);
			} else
				this.watermarkService.update(watermark);
			mv = new JModelAndView("admin/blue/success.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "水印设置成功");
		} else {
			mv = new JModelAndView("admin/blue/error.html.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "您尚未登陆");
		}
		mv.addObject("add_url", CommUtil.getURL(request) + "/admin/album.htm");
		return mv;
	}
}
