package com.metoo.core.tools;

public class RegionUtil {
	
	/**
	 * 通过组织机构code，得到地市的组织编码
	 * @param orgCode   组织机构code
	 * @return
	 */
	public static String getRegionOrgCode(String orgCode) {
		String strSub = orgCode.substring(0,5);
		
		if("84307".equals(strSub))
		{
			return orgCode.substring(0,7)+"00000000";
		}
		else
		{
			return "843000000000000";
		}
         
    }
	
	
	public static void main(String args[]) {
		try {
			String dd = "843073000270000";
			System.out.print(getRegionOrgCode(dd));
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
    

}
