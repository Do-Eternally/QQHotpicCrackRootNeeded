package xjunz.qqhotpiccrack;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;

public class MainActivity extends Activity 
{

	private ProgressDialog mProgressDialog;
	private String mStatusFormat="已初始化：%1$s\n已破解：%2$s";
	private TextView mStatus;
	private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setTitle("正在处理中...");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(false);

		mStatus = findViewById(R.id.tv_status);
		mToolbar = findViewById(R.id.toolbar);
		mToolbar.setTitle("");
		setActionBar(mToolbar);
		mCracker = new CrackImpl(this, new CrackImpl.CrackCallback(){



				@Override
				public void onStart()
				{
					toast("开始");
					mProgressDialog.show();
				}

				@Override
				public void onGetTotalProgress(int total)
				{
					mProgressDialog.setMax(total);
				}

				@Override
				public void onProgress(float fraction)
				{
					mProgressDialog.setProgress((int)(mProgressDialog.getMax() * fraction));
				}

				@Override
				public void onFail(Exception e)
				{
					toast("失败");
				    ByteArrayOutputStream bos=new ByteArrayOutputStream();
					PrintWriter writer=new PrintWriter(bos);
				    e.printStackTrace(writer);
					try
					{
						bos.close();
					}
					catch (IOException ioe)
					{}
					writer.close();
					final String stackTrace=new String(bos.toByteArray());
					new AlertDialog.Builder(MainActivity.this).setTitle("失败").setMessage(stackTrace)
						.setPositiveButton("复制", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								App.copyToClipboard(stackTrace);
							}
						}).show();
					if (mProgressDialog.isShowing())
					{
						mProgressDialog.dismiss();
						mProgressDialog.setProgress(0);
					}
				}

				@Override
				public void onSucceed()
				{
					toast("成功");
					refreshStatus();
					if (mProgressDialog.isShowing())
					{
						mProgressDialog.dismiss();
						mProgressDialog.setProgress(0);
					}
				}
			});
        refreshStatus();


		if (App.SharedPrefManager.isAppUpdate())
		{
			showAbout();
			showHelp();
		}

    }
	
	private void showHelp(){
		new AlertDialog.Builder(this).setTitle("使用须知")
			.setMessage(R.string.help)
			.setPositiveButton("了解", null)
			.setNegativeButton("不恢复会怎样？", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					new AlertDialog.Builder(MainActivity.this)
						.setTitle("不恢复的话")
						.setMessage(R.string.what_if_not_restore)
						.setPositiveButton("了解", null).show();
				}
			})
			.setCancelable(false).show();
	}
	
	private void showAbout(){
		new AlertDialog.Builder(this).setTitle("关于")
			.setMessage(R.string.about)
			.setPositiveButton("捐赠", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					startActivityGivenUri("alipayqr://platformapi/startapp?saId=10000007&qrcode=HTTPS://QR.ALIPAY.COM/FKX05073OYERBA4DWIZYF6","操作失败，感谢支持");
				}
			})
			.setNegativeButton("联系作者", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					startActivityGivenUri("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=561721325&card_type=group&source=qrcode","操作失败");
				}
			}).setNeutralButton("查看源代码", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					startActivityGivenUri("https://github.com/xjunz/QQHotpicCrackRootNeeded","操作失败");
				}
			}).show();
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main,menu);
		return super.onCreateOptionsMenu(menu);
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case R.id.item_about:
				showAbout();
				break;
			case R.id.item_help:
				showHelp();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

	private void refreshStatus()
	{
		mStatus.setText(String.format(mStatusFormat, mCracker.hasCrackedFileBuilt(), mCracker.hasCracked()));
	}

	private void toast(Object obj)
	{
		if (obj == null)
		{
			obj = "[null]";
		}
		Toast.makeText(this, obj.toString(), 0).show();
	}

	private CrackImpl mCracker;
	public void startCrack(View view)
	{

		if (mCracker.hasCracked())
		{
			new AlertDialog.Builder(this).setMessage("貌似已经破解成功了，是否仍然继续？")
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						mCracker.injectCrack();
					}
				}).show();
		}
		else if (!mCracker.hasCrackedFileBuilt())
		{
			toast("请先初始化");
		}
		else
		{
			mCracker.injectCrack();
		}


	}

	public void restore(View view)
	{
		if (!mCracker.hasCrackedFileBuilt())
		{
			toast("请先初始化");
		}
		else if (!mCracker.hasCracked())
		{
			new AlertDialog.Builder(this).setMessage("已经恢复过了，仍然继续？")
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						mCracker.restoreSource();
					}
				}).show();
		}
		else
		{
			mCracker.restoreSource();
		}

    }


	public void init(View view)
	{
		if (mCracker.hasCrackedFileBuilt())
		{
			new AlertDialog.Builder(this).setMessage("已经初始化成功了，仍然继续？")
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						mCracker.init();
					}
				}).show();
		}
		else
		{
			mCracker.init();
		}

	}


	public void startActivityGivenUri(String strUri,CharSequence toastMsgWhenFail){
		try{
			startActivity(new Intent().setData(Uri.parse(strUri)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}catch(Exception e){
			toast(toastMsgWhenFail);
		}
	}
}
