package activity;

import helper.PhotoHelper;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.chinglimchan.circleimageview.R;

public class MainActivity extends Activity {
	
	private Button btnTakePhoto;
	
	private Button btnChoosePhoto;
	
	private CircleImageView imgPhoto;
	
	private Uri imgUri;
	
	public static final int TAKE_PHOTO = 1000;
	public static final int CROP_PHOTO = 1001;
	public static final int CHOOSE_PHOTO = 1002;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
		btnChoosePhoto = (Button) findViewById(R.id.btn_choose_photo);
		imgPhoto = (CircleImageView) findViewById(R.id.img);
		
		imgPhoto.setImageResource(R.drawable.hugh);
		
		btnTakePhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				imgUri = PhotoHelper.createAndGetPhotoUri("output_image.jpg");
				
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);// 指定图片输出地址
				startActivityForResult(intent, TAKE_PHOTO);// 启动相机程序
			}
		});
		
		btnChoosePhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("android.intent.action.GET_CONTENT");
				intent.setType("image/*");
				startActivityForResult(intent, CHOOSE_PHOTO);// 打开相册
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				
				try {
					
					// 将output_image.jpg照片解析成Bitmap对象
					Bitmap bitmap = BitmapFactory.decodeStream(
							getContentResolver().openInputStream(imgUri));
					
					imgPhoto.setImageBitmap(bitmap);// 将裁剪后的照片显示出来
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
				// 真机测试三星s7562i Android4.1.2 无法执行
				// 模拟器三星Galaxy Note 3 Android 4.3 可以执行
				// TODO compatibility test
				// 暂且注释
				Intent intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(imgUri, "image/*");
				intent.putExtra("scale", true);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
				startActivityForResult(intent, CROP_PHOTO);// 启动剪裁程序
			}
			break;

		case CROP_PHOTO:
			if (resultCode == RESULT_OK) {
				try {
					
					// 将output_image.jpg照片解析成Bitmap对象
					Bitmap bitmap = BitmapFactory.decodeStream(
							getContentResolver().openInputStream(imgUri));
					
					imgPhoto.setImageBitmap(bitmap);// 将裁剪后的照片显示出来
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			break;
			
		case CHOOSE_PHOTO:
			if (resultCode == RESULT_OK) {
				
				displayImage(PhotoHelper.handleImage(MainActivity.this, data));
			}
			break;
		default:
			break;
		}
	}

	private void displayImage(String imgPath) {
		if (imgPath != null) {
			Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
			imgPhoto.setImageBitmap(bitmap);
		} else {
			Toast.makeText(MainActivity.this, "failed to get image", Toast.LENGTH_SHORT).show();
		}
	}
}
