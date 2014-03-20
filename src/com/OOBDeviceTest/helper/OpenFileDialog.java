// filename: OpenFileDialog.java
package com.OOBDeviceTest.helper;
import com.OOBDeviceTest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/*
 * Function:鐢ㄤ簬閫夋嫨鏂囦欢 鐨勫璇濇銆�
 * 
 */

public class OpenFileDialog {
	public static String tag = "OpenFileDialog";
	static final public String sRoot = "/";
	static final public String sParent = "..";
	static final public String sFolder = ".";
	static final public String sEmpty = "";
	static final private String sOnErrorMsg = "No rights to access!";

	// 鍙傛暟璇存槑
	// context:涓婁笅鏂�
	// dialogid:瀵硅瘽妗咺D
	// title:瀵硅瘽妗嗘爣棰�
	// callback:涓�釜浼犻�Bundle鍙傛暟鐨勫洖璋冩帴鍙�
	// suffix:闇�閫夋嫨鐨勬枃浠跺悗缂�紝姣斿闇�閫夋嫨wav銆乵p3鏂囦欢鐨勬椂鍊欒缃负".wav;.mp3;"锛屾敞鎰忔渶鍚庨渶瑕佷竴涓垎鍙�;)
	// images:鐢ㄦ潵鏍规嵁鍚庣紑鏄剧ず鐨勫浘鏍囪祫婧怚D銆�
	// 鏍圭洰褰曞浘鏍囩殑绱㈠紩涓簊Root;
	// 鐖剁洰褰曠殑绱㈠紩涓簊Parent;
	// 鏂囦欢澶圭殑绱㈠紩涓簊Folder;
	// 榛樿鍥炬爣鐨勭储寮曚负sEmpty;
	// 鍏朵粬鐨勭洿鎺ユ牴鎹悗缂�繘琛岀储寮曪紝姣斿.wav鏂囦欢鍥炬爣鐨勭储寮曚负"wav"
	public static Dialog createDialog(int id, Context context, String title,
			CallbackBundle callback, String suffix, Map<String, Integer> images) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(new FileSelectView(context, id, callback, suffix,
				images));
		Dialog dialog = builder.create();
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(title);
		return dialog;
	}

	static class FileSelectView extends ListView implements OnItemClickListener {

		private CallbackBundle callback = null;
		private String path = sRoot;
		private List<Map<String, Object>> list = null;
		private int dialogid = 0;

		private String suffix = null;

		private Map<String, Integer> imagemap = null;

		public FileSelectView(Context context, int dialogid,
				CallbackBundle callback, String suffix,
				Map<String, Integer> images) {
			super(context);
			this.imagemap = images;
			this.suffix = suffix == null ? "" : suffix.toLowerCase();
			this.callback = callback;
			this.dialogid = dialogid;
			this.setOnItemClickListener(this);
			refreshFileList();
		}

		private String getSuffix(String filename) {
			int dix = filename.lastIndexOf('.');
			if (dix < 0) {
				return "";
			} else {
				return filename.substring(dix + 1);
			}
		}

		private int getImageId(String s) {
			if (imagemap == null) {
				return 0;
			} else if (imagemap.containsKey(s)) {
				return imagemap.get(s);
			} else if (imagemap.containsKey(sEmpty)) {
				return imagemap.get(sEmpty);
			} else {
				return 0;
			}
		}

		private int refreshFileList() {
			// 鍒锋柊鏂囦欢鍒楄〃
			File[] files = null;
			try {
				files = new File(path).listFiles();
			} catch (Exception e) {
				files = null;
			}
			if (files == null) {
				// 璁块棶鍑洪敊
				Toast.makeText(getContext(), sOnErrorMsg, Toast.LENGTH_SHORT)
						.show();
				return -1;
			}
			if (list != null) {
				list.clear();
			} else {
				list = new ArrayList<Map<String, Object>>(files.length);
			}

			// 鐢ㄦ潵鍏堜繚瀛樻枃浠跺す鍜屾枃浠跺す鐨勪袱涓垪琛�
			ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
			ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();

			if (!this.path.equals(sRoot)) {
				// 娣诲姞鏍圭洰褰�鍜�涓婁竴灞傜洰褰�
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", sRoot);
				map.put("path", sRoot);
				map.put("img", getImageId(sRoot));
				list.add(map);

				map = new HashMap<String, Object>();
				map.put("name", sParent);
				map.put("path", path);
				map.put("img", getImageId(sParent));
				list.add(map);
			}

			for (File file : files) {
				if (file.isDirectory() && file.listFiles() != null) {
					// 娣诲姞鏂囦欢澶�
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", file.getName());
					map.put("path", file.getPath());
					map.put("img", getImageId(sFolder));
					lfolders.add(map);
				} else if (file.isFile()) {
					// 娣诲姞鏂囦欢
					String sf = getSuffix(file.getName()).toLowerCase();
					if (suffix == null
							|| suffix.length() == 0
							|| (sf.length() > 0 && suffix.indexOf("." + sf
									+ ";") >= 0)) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("name", file.getName());
						map.put("path", file.getPath());
						map.put("img", getImageId(sf));
						lfiles.add(map);
					}
				}
			}

			list.addAll(lfolders); // 鍏堟坊鍔犳枃浠跺す锛岀‘淇濇枃浠跺す鏄剧ず鍦ㄤ笂闈�
			list.addAll(lfiles); // 鍐嶆坊鍔犳枃浠�

			SimpleAdapter adapter = new SimpleAdapter(
					getContext(),
					list,
					R.layout.filedialogitem,
					new String[] { "img", "name", "path" },
					new int[] { R.id.filedialogitem_img,
							R.id.filedialogitem_name, R.id.filedialogitem_path });
			this.setAdapter(adapter);
			return files.length;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			// 鏉＄洰閫夋嫨
			String pt = (String) list.get(position).get("path");
			String fn = (String) list.get(position).get("name");
			if (fn.equals(sRoot) || fn.equals(sParent)) {
				// 濡傛灉鏄牴鐩綍鎴栬�涓婁竴灞�
				File fl = new File(pt);
				String ppt = fl.getParent();
				if (ppt != null) {
					// 杩斿洖涓婁竴灞�
					path = ppt;
				} else {
					// 杩斿洖鏍圭洰褰�
					path = sRoot;
				}
			} else {
				File fl = new File(pt);
				if (fl.isFile()) {
					// 濡傛灉鏄枃浠�
					((Activity) getContext()).dismissDialog(this.dialogid); // 璁╂枃浠跺す瀵硅瘽妗嗘秷澶�

					// 璁剧疆鍥炶皟鐨勮繑鍥炲�
					Bundle bundle = new Bundle();
					bundle.putString("path", pt);
					bundle.putString("name", fn);
					// 璋冪敤浜嬪厛璁剧疆鐨勫洖璋冨嚱鏁�
					this.callback.callback(bundle);
					return;
				} else if (fl.isDirectory()) {
					// 濡傛灉鏄枃浠跺す
					// 閭ｄ箞杩涘叆閫変腑鐨勬枃浠跺す
					path = pt;
				}
			}
			this.refreshFileList();
		}
	}
	
	public interface CallbackBundle {
		abstract void callback(Bundle bundle);
	}
}
