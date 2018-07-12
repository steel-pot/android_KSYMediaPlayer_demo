package com.gary.play;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.dyhdyh.widget.loading.bar.LoadingBar;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYMediaRecorder;
import com.ksyun.media.player.KSYTextureView;
import com.ksyun.media.player.KSYVideoView;
import com.ksyun.media.player.misc.KSYProbeMediaInfo;
import com.ksyun.media.player.recorder.KSYMediaRecorderConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //状态
    private boolean inPlay = false;
    private boolean inRecord = false;

    //获取录制对象,为防止出错,每次都重新新创建
    private KSYMediaRecorder mKSYMediaRecorder;

    private void record() {
        if (!inPlay) {
            showMsg("必须在播放时才可以录制!");
            return;
        }
        if (inRecord) {

            stopRecord();
            return;
        }
        inRecord = true;
        KSYMediaRecorderConfig ksyMediaRecorderConfig = new KSYMediaRecorderConfig();
        ksyMediaRecorderConfig.setVideoBitrate(800 * 1000); //码率设置为 800kbps
        ksyMediaRecorderConfig.setKeyFrameIntervalSecond(3); //关键帧间隔为 3s
        ksyMediaRecorderConfig.setAudioBitrate(64 * 1000); // 音频编码码率设置为 64kbps
        ksyMediaRecorderConfig.setAudioRecordState(true);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");// HH:mm:ss
//获取当前时间
        Date date = new Date(System.currentTimeMillis());
        String str = simpleDateFormat.format(date);



            mInPath =mDCIM+ str + ".mp4";


        mKSYMediaRecorder = new KSYMediaRecorder(ksyMediaRecorderConfig, mInPath);

        try {
            mKSYMediaRecorder.init(ksyVideoView.getMediaPlayer()); // 初始化
            mKSYMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("录制出错," + e.getMessage());
            stopRecord();
            return;
        }
        mRecoderButton.setText("停止录制");
    }

    String mInPath;
    String mDCIM=  Environment.getExternalStorageDirectory()
            + File.separator + Environment.DIRECTORY_DCIM
                    +File.separator+"Camera"+File.separator;
    private void stopRecord() {
        inRecord = false;
        //showMsg("文件保存在," + mInPath);
        Toast.makeText(MainActivity.this, "文件保存在," + mInPath, Toast.LENGTH_SHORT).show();

        mKSYMediaRecorder.stop();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(mInPath))));
        mRecoderButton.setText("开始录制");
    }

    private void showMsg(String msg) {
        new AlertDialog.Builder(this).setTitle("提示").setMessage(msg).setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    private KSYTextureView ksyVideoView;
    // 播放SDK提供的监听器

    String mVideoUrl = "http://t.cn/Rd3tUqE";
    // 播放完成时会发出onCompletion回调
    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            inPlay = false;
            // 播放完成，用户可选择释放播放器
            if (ksyVideoView != null) {
                ksyVideoView.stop();
                //ksyMediaPlayer.release();
                //不释放,因为接下来要用
            }
        }
    };

    // 播放器遇到错误时会发出onError回调
    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            hideLoading();
            inPlay = false;
            showMsg("加载出错....");
            //Toast.makeText(MainActivity.this, "加载出错....", Toast.LENGTH_LONG).show();
            return false;
        }
    };
    private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {

            return false;
        }
    };
    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangeListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {

        }
    };
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompletedListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {

        }
    };

    //纯粹为了显示好看点的loading
    private View mParent;


    //录制按钮
    private Button mRecoderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //新建一个File，传入文件夹目录
        File file = new File(mDCIM);
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            file.mkdirs();
        }

        ksyVideoView = this.findViewById(R.id.ksy_videoview);
        ksyVideoView.setDecodeMode(KSYMediaPlayer.KSYDecodeMode.KSY_DECODE_MODE_SOFTWARE);
        ksyVideoView.setOnCompletionListener(mOnCompletionListener);
        ksyVideoView.setOnPreparedListener(mOnPreparedListener);
        ksyVideoView.setOnInfoListener(mOnInfoListener);
        ksyVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangeListener);
        ksyVideoView.setOnErrorListener(mOnErrorListener);
        ksyVideoView.setOnSeekCompleteListener(mOnSeekCompletedListener);

        //显示loading
        mParent = ksyVideoView;


        mRecoderButton = this.findViewById(R.id.btn_record);
        mRecoderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //开始录制
                record();
            }
        });
       this.findViewById(R.id.btn_showlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inGetList)
                {
                    showMsg("更新列表中");
                    return;
                }
                showList();
            }
        });

        this.findViewById(R.id.btn_getlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inGetList)
                {
                    showMsg("更新列表中");
                    return;
                }
                showLoading();
                getList();
            }
        });
       // play(mVideoUrl);
        showLoading();
        getList();

    }

    private void play(final String playUrl) {
        showLoading();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String url = get302(playUrl, 0, 3);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ksyVideoView.stop();
                            ksyVideoView.reset();

                            try {
                                ksyVideoView.setDataSource(url);
                                ksyVideoView.prepareAsync();
                            } catch (IOException e) {
                                e.printStackTrace();
                                showMsg("播放出错");
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    //显示loading
    private void showLoading() {
        CustomLoadingFactory factory = new CustomLoadingFactory();
        LoadingBar.make(mParent, factory).show();

    }

    //隐藏loading
    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadingBar.cancel(mParent);
            }
        });
    }

    private String get302(String urlSrc, int count, int max)   {
        try {
            URL url = null;

            url = new URL(urlSrc);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(3000);
            int code = urlConnection.getResponseCode();
            if (code == 302) {
                String location = urlConnection.getHeaderField("Location");
                count++;
                if (max > 0 && count > (max - 1)) {
                    throw new Exception("重定向次数超过限制!");
                }
                urlSrc = get302(location, count, max);
            } else {
                urlSrc = urlConnection.getURL().toString();
            }
        }catch (Exception ex){}
        return urlSrc;
    }


    // 播放器在准备完成，可以开播时会发出onPrepared回调
    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            if (ksyVideoView != null) {
                hideLoading();
                inPlay = true;
                // 设置视频伸缩模式，此模式为裁剪模式
                ksyVideoView.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                // 开始播放视频
                ksyVideoView.start();

            }
        }
    };


    private void getList()
    {
        inGetList=true;
        new GetList().getList(new GetList.Event() {
            @Override
            public void onGetList(final JSONArray arr) {
                hideLoading();
                if(arr==null)
                {
                    hideLoading();
                    showMsg("获取列表出错!");
                }else{
                    hideLoading();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createDialogList(arr);
                        }
                    });

                }
                inGetList=false;
            }
        });
    }
    AlertDialog  mDialogList  ;
    boolean inGetList=false;
    private void showList()
    {
        if(inGetList)
        {
            showMsg("获取列表中,请稍后!");
            return;
        }
        mDialogList.show();
    }
    private void createDialogList(JSONArray arr)
    {
        final List<Map<String,Object>> mapListJson =getList(arr);

           View bottomView = View.inflate(this,R.layout.dialog_listview,null);//填充ListView布局
          ListView lv = (ListView) bottomView.findViewById(R.id.lv_lives);//初始化ListView控件
          SimpleAdapter simple=new SimpleAdapter(this, mapListJson,
                R.layout.dialog_listview_item, new String[]{"name"}, new int[]{R.id.tv_itme_title });
          lv.setAdapter(simple);
          lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                  mDialogList.hide();
                  if(inRecord)
                  {
                      showMsg("正在录制,请先停止录制!");
                      return ;
                  }
                  Map<String,Object>clickData=mapListJson.get(i);
                  play(clickData.get("url").toString());
              }
          });
        mDialogList=new AlertDialog.Builder(this)
                .setTitle("选择内容").setView(bottomView)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mDialogList.hide();
                    }
                }).create();
    }

    public static Map<String, Object> getMap(String jsonString)

    {

        JSONObject jsonObject;

        try

        {

            jsonObject = new JSONObject(jsonString);   @SuppressWarnings("unchecked")

        Iterator<String> keyIter = jsonObject.keys();

            String key;

            Object value;

            Map<String, Object> valueMap = new HashMap<String, Object>();

            while (keyIter.hasNext())

            {

                key = (String) keyIter.next();

                value = jsonObject.get(key);

                valueMap.put(key, value);

            }

            return valueMap;

        }

        catch (JSONException e)

        {

            e.printStackTrace();

        }

        return null;

    }



    /**

     * 把json 转换为ArrayList 形式

     * @return

     */

    public static List<Map<String, Object>> getList(JSONArray jsonArray)

    {

        List<Map<String, Object>> list = null;

        try

        {



            JSONObject jsonObject;

            list = new ArrayList<Map<String, Object>>();

            for (int i = 0; i < jsonArray.length(); i++)

            {

                jsonObject = jsonArray.getJSONObject(i);

                list.add(getMap(jsonObject.toString()));

            }

        }

        catch (Exception e)

        {

            e.printStackTrace();

        }

        return list;

    }

    @Override
    protected void onStop() {
        super.onStop();
        ksyVideoView.stop();
    }
}
