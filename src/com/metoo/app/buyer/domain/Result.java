package com.metoo.app.buyer.domain;

import java.io.Serializable;

public class Result implements Serializable{
	
	   // 响应业务状态 2020/8/20
		private Integer code;//0:成功  1：失败   如果是移动端 -100：token失效
	 
		// 响应消息
		private String msg;
	 
		// 响应中的数据
		private Object data;

		public Result() {
			super();
			// TODO Auto-generated constructor stub
		}

	
		public Result(Integer code) {
			super();
			this.code = code;
		}


		public Result(Integer code, String msg) {
			super();
			this.code = code;
			this.msg = msg;
		}


		public Result(Integer code, Object data) {
			super();
			this.code = code;
			this.data = data;
		}


		public Result(Integer code, String msg, Object data) {
			super();
			this.code = code;
			this.msg = msg;
			this.data = data;
		}


		public Integer getCode() {
			return code;
		}

		public void setCode(Integer code) {
			this.code = code;
		}

		public String getMsg() {
			return msg;
		}


		public void setMsg(String msg) {
			this.msg = msg;
		}


		public Object getData() {
			return data;
		}


		public void setData(Object data) {
			this.data = data;
		}

		@Override
		public String toString() {
			return "Result [code=" + code + ", msg=" + msg + ", data=" + data + "]";
		}

	
}
