package com.metoo.app.test;

import java.io.StringWriter;
import java.util.Date;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

public class EmailTest {

	public static void main(String[] args) throws Exception {

        VelocityEngine velocityEngine = new VelocityEngine();

        velocityEngine.init();

        Velocity.init();

        /* lets make a Context and put data into it */

        VelocityContext context = new VelocityContext();

        context.put("name", "Velocity");
        context.put("project", "Jakarta");
        context.put("now", new Date());
        context.put("dateFormatUtils", new org.apache.commons.lang.time.DateFormatUtils());

        /* lets make our own string to render */

        String str = "We are using $project $name to render this. 中文测试  $!dateFormatUtils.format($!now,'yyyy-MM-dd')";
        StringWriter stringWriter = new StringWriter();
        Velocity.evaluate(context, stringWriter, "mystring", str);
        System.out.println(" string : " + stringWriter);
	}
}
