package helper;

import java.io.File;
import java.io.IOException;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;

/**
 * 封装图片操作
 * @author chenql
 */
public class PhotoHelper {
	
	/**
	 * 创建File对象保存拍照的图片，名称为fileName，
	 * 将File对象转换成Uri对象并返回
	 * @param fileName
	 * @return 
	 */
	public static Uri createAndGetPhotoUri(String fileName) {
		
		// (权限：WRITE_EXTERNAL_STORAGE)
		File outputImage = new File(
				Environment.getExternalStorageDirectory(),// 获取SD卡根目录
				"output_image.jpg");// 创建File对象，用于存储拍照后的图片
		
		try {
			if (outputImage.exists()) {
				outputImage.delete();
			}
			outputImage.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 将File对象转换成Uri对象并返回，该对象标识着output_image.jpg的唯一地址
		return Uri.fromFile(outputImage);
	}

	/**
	 * 获取图片真实路径
	 * @param context
	 * @param externalContentUri
	 * @param selection
	 * @return
	 */
	public static String getImagePath(Context context, Uri externalContentUri, String selection) {
		
		String path = null;
		
		// 通过Uri和selection来获取真实的图片路径
		Cursor cursor = context.getContentResolver()
				.query(externalContentUri, null, selection, null, null);
		
		if (cursor != null && cursor.moveToFirst()) {
			
			path = cursor.getString(cursor.getColumnIndex(Media.DATA));
			cursor.close();
			
		} else {
			cursor.close();
		}
		return path;
	}
	
	/**
	 * 返回从相册中选择的图片的路径
	 * @param context
	 * @param data
	 * @return
	 */
	public static String handleImage(Context context, Intent data) {
		// 判断手机版本号
		if (Build.VERSION.SDK_INT >= 19) {
			// TODO compatibility test
			// 4.4及以上系统使用方法
			// 从Android 4.4版本开始，选取相册图片不在返回真实的Uri，而是一个封装过的Uri
			// 所以4.4版本以上的手机就要对这个Uri解析
			return handleImageOnKitKat(context, data);
		} else {
			// 4.4及以下系统使用方法
			return handleImageBeforeKitKat(context, data);
		}
	}
	
	/**
	 * 返回从相册种选择的照片的路径，Android 4.4及以上系统使用这个方法；
	 * 从Android 4.4版本开始，选取相册图片不在返回真实的Uri，而是一个封装过的Uri，
	 * 所以4.4版本以上的手机就要对这个Uri解析。
	 * @param context
	 * @param data
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String handleImageOnKitKat(Context context, Intent data) {
		String imgPath = null;
		Uri uri = data.getData();
		if (DocumentsContract.isDocumentUri(context, uri)) {
			
			// 如果是document类型的Uri，则通过document id处理
			String docId = DocumentsContract.getDocumentId(uri);
			
			if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
				
				String id = docId.split(":")[1];// 解析出数字格式的id
				String selection = MediaStore.Images.Media._ID + "=" + id;
				imgPath = PhotoHelper.getImagePath(context, 
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
				
			} else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
				
				Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
				imgPath = PhotoHelper.getImagePath(context, contentUri, null);
			}
		} else if ("content".equalsIgnoreCase(uri.getScheme())) {
			
			// 如果不是document类型的Uri，则使用普通方式处理
			imgPath = PhotoHelper.getImagePath(context, uri, null);
		}
		
		return imgPath;
	}
	
	/**
	 * 返回从相册种选择的照片的路径，Android 4.4以前的系统使用这个方法；
	 * @param context
	 * @param data
	 * @return
	 */
	public static String handleImageBeforeKitKat(Context context, Intent data) {
		Uri uri = data.getData();
		String imgPath = PhotoHelper.getImagePath(context, uri, null);
		return imgPath;
	}
}
