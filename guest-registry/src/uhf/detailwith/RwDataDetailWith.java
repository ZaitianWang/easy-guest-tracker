package uhf.detailwith;

import uhf.linkage.Linkage;
import uhf.structures.OnRwListener;
import uhf.structures.RwData;

public class RwDataDetailWith implements OnRwListener {

	private static RwDataDetailWith rwDataDetailWith = new RwDataDetailWith();

	public static RwDataDetailWith getInstance() {
		return rwDataDetailWith;
	}

	public static RwData rw = new RwData();
	public static int flag = 0;

	@Override
	public void getRwData(RwData rwData) {
		System.out.println(rwData.toString());
		if (null != rwData) {
			flag = 1;
			rw = rwData;
		}
	}

	public void setListener(Linkage linkage) {
		linkage.setOnRwListener(this);
	}

}
