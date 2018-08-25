package xjunz.qqhotpiccrack;

import android.content.*;
import android.content.pm.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import android.os.*;
import com.jaredrummler.android.shell.*;

public class CrackImpl
{

	private static final byte[] BUFFER = new byte[4 * 1024 * 1024];
	private CrackCallback mCallback;
	private static final String DEST_DEX="classes4.dex";
	private static final String DEST_PACKAGE_NAME="com.tencent.mobileqq";
	private static final String ZIPPED_ASSET_CRACK_PACK="crack_pack";
	private static final String APK_BACKUP="backup";
	private static final String APK_CRACKED="cracked";
	private static final String APK_TEMP="temp";

	private Context mContext;
	private static String mDestSourcePath;

	private Handler mHandler;


    public interface CrackCallback
	{
		public static final int START=0;
		public static final int FAIL=1;
		public static final int TOTAL=2;
		public static final int PROGRESS=3;
		public static final int SUCCEED=4;

		void onStart();
		void onGetTotalProgress(int total);
		void onProgress(float fraction);
		void onFail(Exception e);
		void onSucceed();

	}

	public CrackImpl(Context context, CrackCallback callback)
	{
		this.mContext = context;
		this.mHandler = new Handler(context.getMainLooper()){
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case CrackCallback.START:
						mCallback.onStart();
						break;
					case CrackCallback.TOTAL:
						mCallback.onGetTotalProgress(msg.obj);
						break;
					case CrackCallback.PROGRESS:
						mCallback.onProgress(msg.obj);
						break;

					case CrackCallback.FAIL:
						mCallback.onFail((Exception)msg.obj);
						break;
					case CrackCallback.SUCCEED:
						mCallback.onSucceed();
						break;
				}
			}
		};
		this.mCallback = callback;
		try
		{
			ApplicationInfo info=context.getPackageManager().getApplicationInfo(DEST_PACKAGE_NAME, 0);

			mDestSourcePath = info.sourceDir;

		}
		catch (PackageManager.NameNotFoundException e)
		{}
	}

	private void sendMessage(int what, Object obj)
	{
		Message msg=new Message();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}

	private void sendMessage(int what)
	{
		Message msg=new Message();
		msg.what = what;
		mHandler.sendMessage(msg);
	}

	private boolean  exractCrackPack()
	{
		try
		{
			InputStream in=mContext.getAssets().open(ZIPPED_ASSET_CRACK_PACK);
			FileOutputStream out=mContext.openFileOutput(ZIPPED_ASSET_CRACK_PACK, Context.MODE_PRIVATE);
			transferStream(in, out);
			ZipInputStream zipIn=new ZipInputStream(mContext.openFileInput(ZIPPED_ASSET_CRACK_PACK));
			ZipEntry entry=zipIn.getNextEntry();
			if (entry != null)
			{
				transferStream(zipIn, mContext.openFileOutput(DEST_DEX, Context.MODE_PRIVATE));
			}
		}
		catch (IOException e)
		{
			sendMessage(CrackCallback.FAIL, e);
			return false;
		}
		return true;
	}



    private  void transferStream(InputStream input, OutputStream output) throws IOException
	{
        int bytesRead;
        while ((bytesRead = input.read(BUFFER)) != -1)
		{
            output.write(BUFFER, 0, bytesRead);
        }
		input.close();
		output.close();
    }

	private  void transferStreamNoCloseStream(InputStream input, OutputStream output) throws IOException
	{
        int bytesRead;
        while ((bytesRead = input.read(BUFFER)) != -1)
		{
            output.write(BUFFER, 0, bytesRead);
        }
    }


	private boolean backupOriginalAPK()
	{
		if (new File(mContext.getFilesDir() + File.separator + APK_BACKUP).exists())
		{
			return true;
		}

		try
		{
			Shell.SU.run("chmod 666 " + mDestSourcePath);
			File source=new File(mDestSourcePath);
			App.SharedPrefManager.setSourceFileLength(source.length());
			FileInputStream in=new FileInputStream(source);
			transferStream(in, mContext.openFileOutput(APK_BACKUP, Context.MODE_PRIVATE));
		}
		catch (IOException e)
		{
			sendMessage(CrackCallback.FAIL, e);
			return false;
		}
		return true;
	}


    private  boolean buildCrackedFile()
	{


        try
		{
			long approximateTotalLength=new File(mDestSourcePath).length();

			String sourceParentDir=new File(mDestSourcePath).getParentFile().getPath();
			Shell.SU.run("chmod 666 " + sourceParentDir);

			ZipFile src = new ZipFile(mDestSourcePath);
			ZipOutputStream dest = new ZipOutputStream(mContext.openFileOutput(APK_TEMP, Context.MODE_PRIVATE));
			Enumeration<? extends ZipEntry> entries = src.entries();
			File temp=new File(mContext.getFilesDir() + File.separator + APK_TEMP);
			sendMessage(CrackCallback.TOTAL, 100);
			while (entries.hasMoreElements())
			{
				ZipEntry e = entries.nextElement();

				dest.putNextEntry(new ZipEntry(e.getName()));
				if (!e.isDirectory())
				{
					if (e.getName().equals(DEST_DEX))
					{
						transferStreamNoCloseStream(mContext.openFileInput(DEST_DEX), dest);
					}
					else
					{
						transferStreamNoCloseStream(src.getInputStream(e), dest);
					}

					long l=temp.length();
					sendMessage(CrackCallback.PROGRESS, (float)(l * 1.0 / approximateTotalLength));
				}
			}


			dest.closeEntry();
			src.close();
			dest.close();
			App.SharedPrefManager.setCrackedFileLength(temp.length());
            temp.renameTo(new File(mContext.getFilesDir() + File.separator + APK_CRACKED));



		}
		catch (IOException e)
		{
			sendMessage(CrackCallback.FAIL, e);
			return false;
		}

		return true;
    }

	public boolean injectCrack()
	{

		String cracked=mContext.getFilesDir() + File.separator + APK_CRACKED;
		try
		{   Shell.SU.run("chmod 666 " + mDestSourcePath);
			Shell.SU.run("chmod 664 " + cracked);
			transferStream(mContext.openFileInput(APK_CRACKED), new FileOutputStream(mDestSourcePath));
			sendMessage(CrackCallback.SUCCEED);
		}
		catch (IOException e)
		{
			sendMessage(CrackCallback.FAIL, e);
			return false;
		}

		return true;
	}

	public boolean hasCrackedFileBuilt()
	{
		return new File(mContext.getFilesDir() + File.separator + APK_CRACKED).exists();
	}


	public  boolean  hasCracked()
	{

		long s=new File(mDestSourcePath).length();
		long c=App.SharedPrefManager.getCrackedFileLength();
		if (c == s)
		{
			return true;
		}
		return false;
	}

	public void init()
	{

		new Thread(new Runnable(){
				@Override
				public void run()
				{
					sendMessage(CrackCallback.START);
					sendMessage(CrackCallback.TOTAL, 3);
					if (exractCrackPack())
					{
						sendMessage(CrackCallback.PROGRESS, 1 / 2f);

						if (backupOriginalAPK())
						{
							sendMessage(CrackCallback.PROGRESS, 2 / 3f);
						}
						else
						{
							return;
						}
						sendMessage(CrackCallback.PROGRESS, 3 / 3f);

						if (buildCrackedFile())
						{
							sendMessage(CrackCallback.SUCCEED);
						}
						else
						{
							return;
						}
					}
					else
					{
						return;
					}
				}
			}).start();
	}


	public boolean restoreSource()
	{
		String backUp= mContext.getFilesDir() + File.separator + APK_BACKUP;

		try
		{
			Shell.SU.run("chmod 664 " + backUp);
			transferStream(mContext.openFileInput(APK_BACKUP), new FileOutputStream(mDestSourcePath));
		}
		catch (IOException e)
		{
			sendMessage(CrackCallback.FAIL, e);
			return false;
		}
		sendMessage(CrackCallback.SUCCEED);
		return true;
	}



}
