package site.imcu.lcus.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by SHIELD_7 on 2017/8/12.
 * login
 */

public class LoginUtils {
        public static String login(String account ,String password) {
            try {
                String session;
                OkHttpClient client = new OkHttpClient();
                Request requestYzm = new Request.Builder().url("http://jwcweb.lcu.edu.cn/validateCodeAction.do").build();
                Response response = client.newCall(requestYzm).execute();
                byte[] pic = response.body().bytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);

                TessBaseAPI baseApi = new TessBaseAPI();
                baseApi.init("/data/data/site.imcu.lcus/", "urp");
                baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
                Pix pix = ReadFile.readBitmap(bitmap);
                pix = Binarize.sauvolaBinarizeTiled(pix);
                Bitmap after = WriteFile.writeBitmap(pix);
                baseApi.setImage(after);
                String yzm = baseApi.getUTF8Text().replace(" ", "");

                Headers headers = response.headers();
                List<String> cookies = headers.values("Set-Cookie");
                Log.d("info_cookies", "onResponse-size: " + cookies);
                session = cookies.get(0);
                session = session.substring(0, session.indexOf(";"));

                RequestBody body = new FormBody.Builder()
                        .add("zjh",account)
                        .add("mm", password)
                        .add("v_yzm", yzm)
                        .build();
                Request request = new Request.Builder()
                        .url("http://jwcweb.lcu.edu.cn/loginAction.do")
                        .addHeader("cookie", session)
                        .post(body)
                        .build();
                Response responseLogin = client.newCall(request).execute();

                String responseData = responseLogin.body().string();
                Document document = Jsoup.parse(responseData);
                Element element = document.select("title").first();
                if (element.text().equals("学分制综合教务")) {
                    return session;
                } else {
                    return "null";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "null";
            }
        }

        public static String getData(String session, String url){
            try {
                OkHttpClient client = new OkHttpClient.Builder().build();
                Request request1 = new Request.Builder()
                        .url(url)
                        .addHeader("cookie", session)
                        .build();
                Response response = client.newCall(request1).execute();

                String html = response.body().string();
                return html;

            } catch (Exception e) {
                e.printStackTrace();
                return "null";
            }
        }
}
