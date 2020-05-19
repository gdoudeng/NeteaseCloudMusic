package com.imooc.imooc_voice.view.discory.daily;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.imooc.imooc_voice.R;
import com.imooc.imooc_voice.R2;
import com.imooc.imooc_voice.api.HttpConstants;
import com.imooc.imooc_voice.api.RequestCenter;
import com.imooc.imooc_voice.model.newapi.DailyRecommendBean;
import com.imooc.imooc_voice.model.newapi.LoginBean;
import com.imooc.imooc_voice.util.GsonUtil;
import com.imooc.imooc_voice.util.SharePreferenceUtil;
import com.imooc.imooc_voice.util.TimeUtil;
import com.imooc.lib_audio.app.AudioHelper;
import com.imooc.lib_audio.mediaplayer.model.AudioBean;
import com.imooc.lib_common_ui.appbar.AppBarStateChangeListener;
import com.imooc.lib_common_ui.delegate.NeteaseDelegate;
import com.imooc.lib_common_ui.utils.StatusBarUtil;
import com.imooc.lib_image_loader.app.ImageLoaderManager;
import com.imooc.lib_network.listener.DisposeDataListener;


import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DailyRecommendDelegate extends NeteaseDelegate {


	@BindView(R2.id.tv_day)
	TextView mTvDay;
	@BindView(R2.id.tv_month)
	TextView mTvMonth;
	@BindView(R2.id.iv_background)
	ImageView mIvAppBarBackground;
	@BindView(R2.id.iv_background_cover)
	ImageView mIvAppBarCoverBackground;
	@BindView(R2.id.appbar)
	AppBarLayout appBarLayout;
	@BindView(R2.id.rl_play)
	RelativeLayout mRlPlayAll;
	@BindView(R2.id.rv_dailyrecommend)
	RecyclerView mRvRecommend;
	@BindView(R2.id.tv_left_title)
	TextView mTvLeftTitle;

	private DailyRecommendAdapter mAdapter;
	int deltaDistance;
	int minDistance;

	@Override
	public Object setLayout() {
		return R.layout.delegate_daily_recommend;
	}


	@SuppressLint("SetTextI18n")
	@Override
	public void onBindView(@Nullable Bundle savedInstanceState, @NonNull View view) throws Exception {
		ImageLoaderManager manager = ImageLoaderManager.getInstance();
		minDistance = StatusBarUtil.dip2px(getContext(), 55);
		deltaDistance = StatusBarUtil.dip2px(getContext(), 200) - minDistance;

		mTvDay.setText(TimeUtil.getDay(System.currentTimeMillis()));
		mTvMonth.setText("/" +TimeUtil.getMonth(System.currentTimeMillis()));

		String coverUrl = GsonUtil.fromJSON(SharePreferenceUtil.getInstance(getContext()).getUserInfo(""), LoginBean.class).getProfile().getBackgroundUrl();
		//模糊背景
		manager.displayImageForViewGroup(mIvAppBarBackground, coverUrl, 125);
		//正常背景
		manager.displayImageForView(mIvAppBarCoverBackground, coverUrl);
		//获取每日推荐数据
		RequestCenter.getDailyRecommend(new DisposeDataListener() {
			@Override
			public void onSuccess(Object responseObj) {
				DailyRecommendBean bean = (DailyRecommendBean) responseObj;
				List<DailyRecommendBean.RecommendBean> recommend = bean.getRecommend();
				mAdapter = new DailyRecommendAdapter(recommend);
				mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
					@Override
					public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
						DailyRecommendBean.RecommendBean item = (DailyRecommendBean.RecommendBean) adapter.getItem(position);
						String songPlayUrl = HttpConstants.getSongPlayUrl(item.getId());
						AudioHelper.addAudio(getProxyActivity(), new AudioBean(String.valueOf(item.getId()), songPlayUrl, item.getName(), item.getArtists().get(0).getName(), item.getAlbum().getName(), item.getAlbum().getName(), item.getAlbum().getPicUrl(), TimeUtil.getTimeNoYMDH(item.getDuration())));

					}
				});
				mRvRecommend.setAdapter(mAdapter);
				mRvRecommend.setLayoutManager(new LinearLayoutManager(getContext()));
			}

			@Override
			public void onFailure(Object reasonObj) {

			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		initAppBarLayoutListener();
	}

	@OnClick(R2.id.img_daily_back)
	void onClickBack(){
		getSupportDelegate().pop();
	}

	private void initAppBarLayoutListener() {
		appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
			@Override
			public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
				if (state == State.COLLAPSED) {
					setLeftTitleAlpha(255f);
				}
			}

			@Override
			public void onOffsetChanged(AppBarLayout appBarLayout) {
				float alphaPercent = (float) (mRlPlayAll.getTop() - minDistance) / (float) deltaDistance;
				int alpha = (int) (alphaPercent * 255);
				mIvAppBarCoverBackground.setImageAlpha(alpha);
				mTvMonth.setAlpha(alphaPercent);
				mTvDay.setAlpha(alphaPercent);
				if (alphaPercent < 0.2f) {
					float leftTitleAlpha = (1.0f - alphaPercent / 0.2f);
					setLeftTitleAlpha(leftTitleAlpha);
				} else {
					setLeftTitleAlpha(0);
				}
			}
		});
	}

	private void setLeftTitleAlpha(float alpha) {
		mTvLeftTitle.setVisibility(View.VISIBLE);
		mTvLeftTitle.setAlpha(alpha);
	}


	static class DailyRecommendAdapter extends BaseQuickAdapter<DailyRecommendBean.RecommendBean, BaseViewHolder>{

		DailyRecommendAdapter(@Nullable List<DailyRecommendBean.RecommendBean> data) {
			super(R.layout.item_gedan_detail_song, data);
		}

		@Override
		protected void convert(@NonNull BaseViewHolder helper, DailyRecommendBean.RecommendBean item) {
			helper.setText(R.id.item_play_no, String.valueOf(helper.getLayoutPosition()+1));
			helper.setText(R.id.viewpager_list_toptext, item.getName());
			helper.setText(R.id.viewpager_list_bottom_text, item.getArtists().get(0).getName() + " - " + item.getAlbum().getName());
			helper.setOnClickListener(R.id.viewpager_list_button, new View.OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});
		}
	}

}
