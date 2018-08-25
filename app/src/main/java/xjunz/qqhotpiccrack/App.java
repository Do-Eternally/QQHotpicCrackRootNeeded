package xjunz.qqhotpiccrack;
import android.app.*;
import android.widget.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.*;

public class App extends Application
{

	private static Toast mToast;
	private static Handler mMainHandler;
	private static SharedPreferences mSharedPref;
	private static SharedPreferences.Editor mEditor;
	private static int version_code;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		mToast=Toast.makeText(this.getApplicationContext(),"",0);
		mMainHandler=new Handler(getMainLooper());
		mSharedPref=getSharedPreferences("data",MODE_PRIVATE);
		mEditor=mSharedPref.edit();
		try
		{
			PackageInfo info=getPackageManager().getPackageInfo(getPackageName(), 0);
			version_code=info.versionCode;
		}
		catch (PackageManager.NameNotFoundException e)
		{}
	}
	
	
	public static void singleInstanceToast(final Object obj){
		
		mMainHandler.post(new Runnable(){

				@Override
				public void run()
				{
					String str;
					if(obj==null){
						str="(null)";
					}else{
						str=obj.toString();
					}
					mToast.setText(str);
					mToast.show();
				}
			});
		
	}
	
	public static class SharedPrefManager{
		
		private static String key_cracked_file_length="cracked_file_length";
		private static String key_source_file_length="source_file_lengtg";
		private static String key_version_code="version_code";
		public static  long getCrackedFileLength(){
			return mSharedPref.getLong(key_cracked_file_length,-1);
			
		}
		
		public static long getSourceFileLength(){
			return mSharedPref.getLong(key_source_file_length,-1);
		}
		
		public static void setCrackedFileLength(long length){
			mEditor.putLong(key_cracked_file_length,length).commit();
		}
		
		public static void setSourceFileLength(long length){
		    mEditor.putLong(key_source_file_length,length).commit();
		}
		
		public static boolean isAppUpdate(){
			if(version_code>mSharedPref.getInt(key_version_code,-1)){
				mEditor.putInt(key_version_code,version_code).commit();
				return true;
			}
			return false;
		}
	}
	
}
