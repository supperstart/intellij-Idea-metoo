//自定义提示框
window.alert= function(msg){
	var config = {
			fontSize:'14px',
			backgroundColor:'#aaa',
			fontColor:'#FFFFFF',
			borderRadius:'30px',
			border:'solid 1px #aaa',
			padding:'10px'
	};
	var $alertPanel = $("#alertPane");
	if($alertPanel.length>0){
		$alertPanel.find("span").html(msg);
	}else{
		var html = new Array();
		html.push('<div id="alertPane" style="display:none;position:fixed;width:60%;left:20%;bottom:15%;height:30px;text-align:center;z-index:9999999;background-color:'+config.backgroundColor+';color:'+config.fontColor+';border-radius:'+config.borderRadius+';-moz-border-radius:'+config.borderRadius+';-webkit-border-radius:'+config.borderRadius+'">')
		html.push('<span style="padding:'+config.padding+';font-size:'+config.fontSize+';font-weight: bold;line-height:30px;">')
		html.push(msg)
		html.push('</span></div>');
		$alertPanel = $(html.join(''));
		$("body").append($alertPanel);
	}
	
	$alertPanel.stop().fadeIn('fast').delay(5000).fadeOut('slow');
};


function getURLParameter(param, url)
{
	var params = (url.substr(url.indexOf("?") + 1)).split("&");
	if (params != null)
	{
		for(var i=0; i<params.length; i++)
		{
			var strs = params[i].split("=");
			if(strs[0] == param)
			{
				return strs[1];
			}
		} 
	}
	return "";
}