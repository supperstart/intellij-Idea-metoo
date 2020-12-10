package com.metoo.manage.seller.action;

import java.awt.Font;
import java.io.File;
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
import com.metoo.manage.admin.tools.StoreTools;
import com.metoo.view.web.tools.AlbumViewTools;
import com.metoo.view.web.tools.StoreViewTools;

/**
 * @info 卖家相册中心管理控制器
 * @since V1.0
 * @author 沈阳网之商科技有限公司 www.koala.com erikzhang
 * 
 */
@Controller
public class AlbumSellerAction {
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
	private StoreTools storeTools;
	@Autowired
	private StoreViewTools storeViewTools;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ImageTools ImageTools;

	@SecurityMapping(title = "相册列表", value = "/seller/album.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album.htm")
	public ModelAndView album(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/album.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		AlbumQueryObject aqo = new AlbumQueryObject();
		aqo.addQuery("obj.user.id", new SysMap("user_id", user.getId()), "=");
		aqo.setCurrentPage(CommUtil.null2Int(currentPage));
		aqo.setOrderBy("album_sequence");
		aqo.setOrderType("asc");
		IPageList pList = this.albumService.list(aqo);
		String url = this.configService.getSysConfig().getAddress();
		String path = this.storeTools
				.createUserFolder(request, user.getStore());
		double csize = CommUtil.fileSize(new File(path));
		double remainSpace = 0;
		if (user.getStore().getGrade().getSpaceSize() > 0) {
			remainSpace = CommUtil.div(user.getStore().getGrade()
					.getSpaceSize()
					* 1024 - csize, 1024);
			mv.addObject("remainSpace", remainSpace);
		}
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		CommUtil.saveIPageList2ModelAndView(url + "/seller/album.htm", "", "",
				pList, mv);
		mv.addObject("albumViewTools", albumViewTools);
		return mv;
	}

	@SecurityMapping(title = "新增相册", value = "/seller/album_add.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_add.htm")
	public ModelAndView album_add(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/album_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("currentPage", currentPage);
		return mv;
	}

	@SecurityMapping(title = "新增相册", value = "/seller/album_edit.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_edit.htm")
	public ModelAndView album_edit(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String id) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/album_add.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Album obj = this.albumService.getObjById(CommUtil.null2Long(id));
		mv.addObject("obj", obj);
		mv.addObject("currentPage", currentPage);
		return mv;
	}

	@SecurityMapping(title = "相册保存", value = "/seller/album_save.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_save.htm")
	public String album_save(HttpServletRequest request,
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
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		album.setUser(user);
		boolean ret = true;
		if (id.equals("")) {
			ret = this.albumService.save(album);
		} else
			ret = this.albumService.update(album);
		return "redirect:album.htm?currentPage=" + currentPage;
	}

	@SecurityMapping(title = "图片上传", value = "/seller/album_upload.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_upload.htm")
	public ModelAndView album_upload(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/album_upload.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Album> objs = this.albumService
				.query("select obj from Album obj where obj.user.id=:user_id order by obj.album_sequence asc",
						params, -1, -1);
		mv.addObject("objs", objs);
		mv.addObject("currentPage", currentPage);
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

	@SecurityMapping(title = "相册删除", value = "/seller/album_del.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_del.htm")
	public String album_del(HttpServletRequest request, String mulitId) {
		String[] ids = mulitId.split(",");
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		for (String id : ids) {
			if (!id.equals("")) {
				Album album = this.albumService.getObjById(CommUtil
						.null2Long(id));
				if (album != null) {
					Map params = new HashMap();
					params.put("album_id", album.getId());
					List<Accessory> accs = this.accessoryService
							.query("select obj from Accessory obj where obj.album_id=:album_id",
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

	@SecurityMapping(title = "相册封面设置", value = "/seller/album_cover.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_cover.htm")
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

	@SecurityMapping(title = "相册转移", value = "/seller/album_transfer.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_transfer.htm")
	public ModelAndView album_transfer(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String album_id,
			String id) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/album_transfer.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Album> objs = this.albumService
				.query("select obj from Album obj where obj.user.id=:user_id order by obj.album_sequence asc",
						params, -1, -1);
		mv.addObject("objs", objs);
		mv.addObject("currentPage", currentPage);
		mv.addObject("album_id", album_id);
		mv.addObject("mulitId", id);
		return mv;
	}

	@SecurityMapping(title = "图片转移相册", value = "/seller/album_transfer_save.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_transfer_save.htm")
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

	@SecurityMapping(title = "图片列表", value = "/seller/album_image.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_image.htm")
	public ModelAndView album_image(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/album_image.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Album album = this.albumService.getObjById(Long.parseLong(id));
		AccessoryQueryObject aqo = new AccessoryQueryObject();
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		if (id != null && !id.equals("")) {
			aqo.addQuery("obj.album.id",
					new SysMap("album_id", CommUtil.null2Long(id)), "=");
		} else {
			aqo.addQuery("obj.album.id is null", null);
		}
		aqo.addQuery("obj.album.user.id", new SysMap("user_id", user.getId()),
				"=");
		aqo.setCurrentPage(CommUtil.null2Int(currentPage));
		aqo.setPageSize(16);
		aqo.setOrderBy("addTime");
		aqo.setOrderType("desc");
		IPageList pList = this.accessoryService.list(aqo);
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		String path = this.storeTools
				.createUserFolder(request, user.getStore());
		double csize = CommUtil.fileSize(new File(path));
		double remainSpace = 0;
		if (user.getStore().getGrade().getSpaceSize() > 0) {
			remainSpace = CommUtil.div(user.getStore().getGrade()
					.getSpaceSize()
					* 1024 - csize, 1024);
			mv.addObject("remainSpace", remainSpace);
		}
		CommUtil.saveIPageList2ModelAndView(url + "/seller/album_image.htm",
				"", "&id=" + id, pList, mv);
		Map params = new HashMap();
		params.put("user_id", user.getId());
		List<Album> albums = this.albumService
				.query("select obj from Album obj where obj.user.id=:user_id order by obj.album_sequence asc",
						params, -1, -1);
		mv.addObject("albums", albums);
		mv.addObject("album", album);
		return mv;
	}

	@SecurityMapping(title = "图片幻灯查看", value = "/seller/image_slide.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/image_slide.htm")
	public ModelAndView image_slide(HttpServletRequest request,
			HttpServletResponse response, String album_id, String id) {
		ModelAndView mv = new JModelAndView(
				"user/default/sellercenter/image_slide.html",
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

	@SecurityMapping(title = "相册内图片删除", value = "/seller/album_img_del.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_img_del.htm")
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
				this.accessoryService.delete(acc.getId());
			}
		}
		return "redirect:album_image.htm?id=" + album_id + "&currentPage="
				+ currentPage;
	}

	@SecurityMapping(title = "图片转移相册", value = "/seller/album_watermark.htm*", rtype = "seller", rname = "图片空间", rcode = "album_seller", rgroup = "其他管理")
	@RequestMapping("/seller/album_watermark.htm")
	public String album_watermark(HttpServletRequest request, String mulitId,
			String album_id, String to_album_id, String currentPage) {
		Long store_id = null;
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		if (user.getStore() != null) {
			store_id = user.getStore().getId();
		}
		if (store_id != null) {
			WaterMark waterMark = this.waterMarkService.getObjByProperty(null,
					"store.id", store_id);
			if (waterMark != null) {
				String[] ids = mulitId.split(",");
				for (String id : ids) {
					if (!id.equals("")) {
						Accessory acc = this.accessoryService.getObjById(Long
								.parseLong(id));
						String path = request.getSession().getServletContext()
								.getRealPath("/")
								+ acc.getPath()
								+ File.separator
								+ acc.getName();
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
						// 同步生成小图片
						String ext = acc.getExt().indexOf(".") < 0 ? "."
								+ acc.getExt() : acc.getExt();
						String source = request.getSession()
								.getServletContext().getRealPath("/")
								+ acc.getPath()
								+ File.separator
								+ acc.getName();
						source = source.replace("/", "\\");
						String target = source + "_small" + ext;
						CommUtil.createSmall(source, target, this.configService
								.getSysConfig().getSmallWidth(),
								this.configService.getSysConfig()
										.getSmallHeight());
						// 同步生成中等图片
						String midext = acc.getExt().indexOf(".") < 0 ? "."
								+ acc.getExt() : acc.getExt();
						String midtarget = source + "_middle" + ext;
						CommUtil.createSmall(source, midtarget,
								this.configService.getSysConfig()
										.getMiddleWidth(), this.configService
										.getSysConfig().getMiddleHeight());
					}
				}
			}
		}
		return "redirect:album_image.htm?id=" + album_id + "&currentPage="
				+ currentPage;
	}
}
