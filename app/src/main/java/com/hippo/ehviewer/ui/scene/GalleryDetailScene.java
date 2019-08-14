/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.ui.scene;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.transition.TransitionInflater;

import com.axlecho.api.MHApi;
import com.axlecho.api.MHApiSource;
import com.google.gson.Gson;
import com.hippo.android.resource.AttrResources;
import com.hippo.beerbelly.BeerBelly;
import com.hippo.drawable.RoundSideRectDrawable;
import com.hippo.drawerlayout.DrawerLayout;
import com.hippo.ehviewer.AppConfig;
import com.hippo.ehviewer.EhApplication;
import com.hippo.ehviewer.EhDB;
import com.hippo.ehviewer.R;
import com.hippo.ehviewer.UrlOpener;
import com.hippo.ehviewer.client.EhCacheKeyFactory;
import com.hippo.ehviewer.client.EhUtils;
import com.hippo.ehviewer.client.data.GalleryChapter;
import com.hippo.ehviewer.client.data.GalleryChapterGroup;
import com.hippo.ehviewer.client.data.GalleryComment;
import com.hippo.ehviewer.client.data.GalleryDetail;
import com.hippo.ehviewer.client.data.GalleryInfo;
import com.hippo.ehviewer.client.data.ListUrlBuilder;
import com.hippo.ehviewer.dao.ReadingRecord;
import com.hippo.ehviewer.module.DetailViewModule;
import com.hippo.ehviewer.ui.GalleryActivity;
import com.hippo.ehviewer.ui.MainActivity;
import com.hippo.reveal.ViewAnimationUtils;
import com.hippo.ripple.Ripple;
import com.hippo.scene.Announcer;
import com.hippo.scene.TransitionHelper;
import com.hippo.text.Html;
import com.hippo.text.URLImageGetter;
import com.hippo.util.DrawableManager;
import com.hippo.util.ExceptionUtils;
import com.hippo.view.ViewTransition;
import com.hippo.widget.AutoWrapLayout;
import com.hippo.widget.LoadImageView;
import com.hippo.widget.ObservedTextView;
import com.hippo.widget.SimpleGridAutoSpanLayout;
import com.hippo.yorozuya.AssertUtils;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.IntIdGenerator;
import com.hippo.yorozuya.SimpleHandler;
import com.hippo.yorozuya.ViewUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class GalleryDetailScene extends BaseScene implements View.OnClickListener {

    public final static String KEY_ACTION = "action";
    public static final String ACTION_GALLERY_INFO = "action_gallery_info";
    public static final String ACTION_GID_TOKEN = "action_gid_token";
    public static final String KEY_GALLERY_INFO = "gallery_info";
    public static final String KEY_GID = "gid";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_PAGE = "page";
    private static final int REQUEST_CODE_COMMENT_GALLERY = 0;
    private static final int STATE_INIT = -1;
    private static final int STATE_NORMAL = 0;
    private static final int STATE_REFRESH = 1;
    private static final int STATE_REFRESH_HEADER = 2;
    private static final int STATE_FAILED = 3;
    private static final String KEY_GALLERY_DETAIL = "gallery_detail";
    private static final String KEY_REQUEST_ID = "request_id";
    private static final long ANIMATE_TIME = 300L;
    /*---------------
     View life cycle
     ---------------*/
    @Nullable
    private ScrollView mainView;
    @Nullable
    private TextView mTip;
    @Nullable
    private ViewTransition mViewTransition;
    // Header
    @Nullable
    private View mHeader;
    @Nullable
    private View mColorBg;
    @Nullable
    private LoadImageView mThumb;
    @Nullable
    private TextView mTitle;
    @Nullable
    private TextView mUploader;
    @Nullable
    private TextView mCategory;
    @Nullable
    private ImageView mOtherActions;
    @Nullable
    private ViewGroup mActionGroup;
    @Nullable
    private TextView mFavorite;
    @Nullable
    private View mRead;
    // Below header
    @Nullable
    private View mBelowHeader;
    // Info
    @Nullable
    private View mInfo;
    @Nullable
    private TextView mLanguage;
    @Nullable
    private TextView mPages;
    @Nullable
    private TextView mSize;
    @Nullable
    private TextView mPosted;
    @Nullable
    private TextView mFavoredTimes;
    // Actions
    @Nullable
    private View mActions;
    @Nullable
    private TextView mRatingText;
    @Nullable
    private RatingBar mRating;
    @Nullable
    private View mHeartGroup;
    @Nullable
    private TextView mHeart;
    @Nullable
    private TextView mHeartOutline;
    @Nullable
    private TextView mTorrent;
    @Nullable
    private TextView mArchive;
    @Nullable
    private TextView mShare;
    @Nullable
    private TextView mRate;
    @Nullable
    private TextView mSimilar;
    @Nullable
    private TextView mSearchCover;
    // Tags
    @Nullable
    private LinearLayout mTags;
    @Nullable
    private TextView mNoTags;
    // Comments
    @Nullable
    private LinearLayout mComments;
    @Nullable
    private TextView mCommentsText;
    // Intro
    @Nullable
    private LinearLayout mIntro;
    @Nullable
    private TextView mIntroText;
    // Previews
    @Nullable
    private View mPreviews;
    @Nullable
    private SimpleGridAutoSpanLayout mGridLayout;
    @Nullable
    private TextView mPreviewText;
    // Progress
    @Nullable
    private View mProgress;
    @Nullable
    private ViewTransition mViewTransition2;
    @Nullable
    private PopupMenu mPopupMenu;
    @Nullable
    private RadioGroup mSourceBar;

    private DetailViewModule module;
    private int mRequestId = IntIdGenerator.INVALID_ID;

    @State
    private int mState = STATE_INIT;

    private static String getRatingText(float rating, Resources resources) {
        int resId;
        switch (Math.round(rating)) {
            case 0:
                resId = R.string.rating0;
                break;
            case 1:
                resId = R.string.rating1;
                break;
            case 2:
                resId = R.string.rating2;
                break;
            case 3:
                resId = R.string.rating3;
                break;
            case 4:
                resId = R.string.rating4;
                break;
            case 5:
                resId = R.string.rating5;
                break;
            case 6:
                resId = R.string.rating6;
                break;
            case 7:
                resId = R.string.rating7;
                break;
            case 8:
                resId = R.string.rating8;
                break;
            case 9:
                resId = R.string.rating9;
                break;
            case 10:
                resId = R.string.rating10;
                break;
            default:
                resId = R.string.rating_none;
                break;
        }

        return resources.getString(resId);
    }

    @Nullable
    private static String getArtist(GalleryChapterGroup[] tagGroups) {
        if (null == tagGroups) {
            return null;
        }
        for (GalleryChapterGroup tagGroup : tagGroups) {
            if ("artist".equals(tagGroup.getGroupName()) && tagGroup.size() > 0) {
                return tagGroup.getChapterAt(0);
            }
        }
        return null;
    }

    private void handleArgs(Bundle args) {
        if (args == null) {
            return;
        }

        String action = args.getString(KEY_ACTION);
        if (ACTION_GALLERY_INFO.equals(action)) {

            module.setInfo(args.getParcelable(KEY_GALLERY_INFO));

        } else if (ACTION_GID_TOKEN.equals(action)) {
            // TODO DeepLink
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initModule();
        handleArgs(getArguments());
    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        Context context = getContext2();

        if (context == null) {
            return null;
        }

        if (inflater == null) {
            return null;
        }


        View view = inflater.inflate(R.layout.scene_gallery_detail, container, false);

        ViewGroup main = (ViewGroup) ViewUtils.$$(view, R.id.main);
        mainView = (ScrollView) ViewUtils.$$(main, R.id.scroll_view);
        View progressView = ViewUtils.$$(main, R.id.progress_view);
        mTip = (TextView) ViewUtils.$$(main, R.id.tip);
        mSourceBar = (RadioGroup) ViewUtils.$$(view, R.id.source_bar);
        bindSource(mSourceBar);

        mViewTransition = new ViewTransition(mainView, progressView, mTip);


        View actionsScrollView = ViewUtils.$$(view, R.id.actions_scroll_view);
        setDrawerGestureBlocker(new DrawerLayout.GestureBlocker() {
            private void transformPointToViewLocal(int[] point, View child) {
                ViewParent viewParent = child.getParent();

                while (viewParent instanceof View) {
                    View view = (View) viewParent;
                    point[0] += view.getScrollX() - child.getLeft();
                    point[1] += view.getScrollY() - child.getTop();

                    if (view instanceof DrawerLayout) {
                        break;
                    }

                    child = view;
                    viewParent = child.getParent();
                }
            }

            @Override
            public boolean shouldBlockGesture(MotionEvent ev) {
                int[] point = new int[]{(int) ev.getX(), (int) ev.getY()};
                transformPointToViewLocal(point, actionsScrollView);
                return !isDrawersVisible()
                        && point[0] > 0 && point[0] < actionsScrollView.getWidth()
                        && point[1] > 0 && point[1] < actionsScrollView.getHeight();
            }
        });

        Drawable drawable = DrawableManager.getVectorDrawable(context, R.drawable.big_sad_pandroid);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        mTip.setCompoundDrawables(null, drawable, null, null);
        mTip.setOnClickListener(this);

        boolean isDarkTheme = !AttrResources.getAttrBoolean(context, R.attr.isLightTheme);
        mHeader = ViewUtils.$$(mainView, R.id.header);
        mColorBg = ViewUtils.$$(mHeader, R.id.color_bg);
        mThumb = (LoadImageView) ViewUtils.$$(mHeader, R.id.thumb);
        mTitle = (TextView) ViewUtils.$$(mHeader, R.id.title);
        mUploader = (TextView) ViewUtils.$$(mHeader, R.id.uploader);
        mCategory = (TextView) ViewUtils.$$(mHeader, R.id.category);
        mOtherActions = (ImageView) ViewUtils.$$(mHeader, R.id.other_actions);
        mActionGroup = (ViewGroup) ViewUtils.$$(mHeader, R.id.action_card);
        mFavorite = (TextView) ViewUtils.$$(mActionGroup, R.id.download);
        mRead = ViewUtils.$$(mActionGroup, R.id.read);
        Ripple.addRipple(mOtherActions, isDarkTheme);
        Ripple.addRipple(mFavorite, isDarkTheme);
        Ripple.addRipple(mRead, isDarkTheme);
        mUploader.setOnClickListener(this);
        mCategory.setOnClickListener(this);
        mOtherActions.setOnClickListener(this);
        mFavorite.setOnClickListener(this);
        mRead.setOnClickListener(this);


        mBelowHeader = mainView.findViewById(R.id.below_header);
        View belowHeader = mBelowHeader;

        mInfo = ViewUtils.$$(belowHeader, R.id.info);
        mLanguage = (TextView) ViewUtils.$$(mInfo, R.id.language);
        mPages = (TextView) ViewUtils.$$(mInfo, R.id.pages);
        mSize = (TextView) ViewUtils.$$(mInfo, R.id.size);
        mPosted = (TextView) ViewUtils.$$(mInfo, R.id.posted);
        mFavoredTimes = (TextView) ViewUtils.$$(mInfo, R.id.favoredTimes);
        Ripple.addRipple(mInfo, isDarkTheme);
        mInfo.setOnClickListener(this);

        mActions = ViewUtils.$$(belowHeader, R.id.actions);
        mRatingText = (TextView) ViewUtils.$$(mActions, R.id.rating_text);
        mRating = (RatingBar) ViewUtils.$$(mActions, R.id.rating);
        mHeartGroup = ViewUtils.$$(mActions, R.id.heart_group);
        mHeart = (TextView) ViewUtils.$$(mHeartGroup, R.id.heart);
        mHeartOutline = (TextView) ViewUtils.$$(mHeartGroup, R.id.heart_outline);
        mTorrent = (TextView) ViewUtils.$$(mActions, R.id.torrent);
        mArchive = (TextView) ViewUtils.$$(mActions, R.id.archive);
        mShare = (TextView) ViewUtils.$$(mActions, R.id.share);
        mRate = (TextView) ViewUtils.$$(mActions, R.id.rate);
        mSimilar = (TextView) ViewUtils.$$(mActions, R.id.similar);
        mSearchCover = (TextView) ViewUtils.$$(mActions, R.id.search_cover);
        Ripple.addRipple(mHeartGroup, isDarkTheme);
        Ripple.addRipple(mTorrent, isDarkTheme);
        Ripple.addRipple(mArchive, isDarkTheme);
        Ripple.addRipple(mShare, isDarkTheme);
        Ripple.addRipple(mRate, isDarkTheme);
        Ripple.addRipple(mSimilar, isDarkTheme);
        Ripple.addRipple(mSearchCover, isDarkTheme);
        mHeartGroup.setOnClickListener(this);
        mTorrent.setOnClickListener(this);
        mArchive.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mRate.setOnClickListener(this);
        mSimilar.setOnClickListener(this);
        mSearchCover.setOnClickListener(this);

        mTags = (LinearLayout) ViewUtils.$$(belowHeader, R.id.tags);
        mNoTags = (TextView) ViewUtils.$$(mTags, R.id.no_tags);

        mComments = (LinearLayout) ViewUtils.$$(belowHeader, R.id.comments);
        mCommentsText = (TextView) ViewUtils.$$(mComments, R.id.comments_text);
        Ripple.addRipple(mComments, isDarkTheme);
        mComments.setOnClickListener(this);

        mIntro = (LinearLayout) ViewUtils.$$(belowHeader, R.id.intro);
        mIntroText = (TextView) ViewUtils.$$(belowHeader, R.id.intro_text);
        Ripple.addRipple(mIntro, isDarkTheme);

        mPreviews = ViewUtils.$$(belowHeader, R.id.previews);
        mGridLayout = (SimpleGridAutoSpanLayout) ViewUtils.$$(mPreviews, R.id.grid_layout);
        mPreviewText = (TextView) ViewUtils.$$(mPreviews, R.id.preview_text);
        Ripple.addRipple(mPreviews, isDarkTheme);
        mPreviews.setOnClickListener(this);

        mProgress = ViewUtils.$$(mainView, R.id.progress);

        mViewTransition2 = new ViewTransition(mBelowHeader, mProgress);
        return view;
    }

    private void initModule() {
        module = ViewModelProviders.of(this).get(DetailViewModule.class);
        module.getBaseInfo().observe(this, galleryInfo -> {
            if (galleryInfo == null) {
                return;
            }
            // Add to history
            EhDB.putHistoryInfo(galleryInfo);
            currentSource = galleryInfo.source;
            MHApi.Companion.getINSTANCE().select(galleryInfo.source);
            bindViewForInfo(galleryInfo);
            module.detail();
        });

        module.getDetail().observe(this, detail -> {
            if (detail == null) {
                return;
            }

            checkReadInfo(detail);
            adjustViewVisibility(STATE_NORMAL, true);
            bindViewForDetail(detail);
        });
    }

    @Nullable
    @Override
    public View onCreateView2(LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {

        View view = initViews(inflater, container);
        prepareData();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setDrawerGestureBlocker(null);
    }

    private boolean prepareData() {
        Context context = getContext2();
        String gid = module.getGid();
        if (gid.equals("-1")) {
            return false;
        }

        // Get from cache
        GalleryDetail detail = EhApplication.getGalleryDetailCache(context).get(gid);
        if (detail != null) {
            module.getDetail().setValue(detail);
            return true;
        }

        return false;
    }

    private boolean request() {
        module.detail();
        return true;
    }

    private boolean createCircularReveal() {
        if (mColorBg == null) {
            return false;
        }

        int w = mColorBg.getWidth();
        int h = mColorBg.getHeight();
        if (ViewCompat.isAttachedToWindow(mColorBg) && w != 0 && h != 0) {
            Resources resources = getContext2().getResources();
            int keylineMargin = resources.getDimensionPixelSize(R.dimen.keyline_margin);
            int thumbWidth = resources.getDimensionPixelSize(R.dimen.gallery_detail_thumb_width);
            int thumbHeight = resources.getDimensionPixelSize(R.dimen.gallery_detail_thumb_height);

            int x = thumbWidth / 2 + keylineMargin;
            int y = thumbHeight / 2 + keylineMargin;

            int radiusX = Math.max(Math.abs(x), Math.abs(w - x));
            int radiusY = Math.max(Math.abs(y), Math.abs(h - y));
            float radius = (float) Math.hypot(radiusX, radiusY);

            ViewAnimationUtils.createCircularReveal(mColorBg, x, y, 0, radius).setDuration(300).start();
            return true;
        } else {
            return false;
        }
    }

    private void adjustViewVisibility(int state, boolean animation) {
        if (state == mState) {
            return;
        }
        if (mViewTransition == null || mViewTransition2 == null) {
            return;
        }

        int oldState = mState;
        mState = state;

        switch (state) {
            case STATE_NORMAL:
                // Show mMainView
                mViewTransition.showView(0, animation);
                // Show mBelowHeader
                mViewTransition2.showView(0, animation);
                break;
            case STATE_REFRESH:
                // Show mProgressView
                mViewTransition.showView(1, animation);
                break;
            case STATE_REFRESH_HEADER:
                // Show mMainView
                mViewTransition.showView(0, animation);
                // Show mProgress
                mViewTransition2.showView(1, animation);
                break;
            default:
            case STATE_INIT:
            case STATE_FAILED:
                // Show mFailedView
                mViewTransition.showView(2, animation);
                break;
        }

        if ((oldState == STATE_INIT || oldState == STATE_FAILED || oldState == STATE_REFRESH) &&
                (state == STATE_NORMAL || state == STATE_REFRESH_HEADER) && AttrResources.getAttrBoolean(getContext2(), R.attr.isLightTheme)) {
            if (!createCircularReveal()) {
                SimpleHandler.getInstance().post(this::createCircularReveal);
            }
        }
    }

    private void bindViewForInfo(@NotNull GalleryInfo gi) {
        mThumb.load(EhCacheKeyFactory.getThumbKey(gi.gid), gi.thumb);
        mTitle.setText(EhUtils.getSuitableTitle(gi));
        mUploader.setText(gi.uploader);
        mCategory.setText(EhUtils.getCategory(gi.category));
        mCategory.setTextColor(EhUtils.getCategoryColor(gi.category));
        updateFavoriteText();
    }

    private void updateFavoriteDrawable(GalleryDetail gd) {
        if (gd == null) {
            return;
        }
        if (mHeart == null || mHeartOutline == null) {
            return;
        }

        if (gd.isFavorited || EhDB.containLocalFavorites(gd)) {
            mHeart.setVisibility(View.VISIBLE);
            mHeart.setText(R.string.local_favorites);
            mHeartOutline.setVisibility(View.GONE);
        } else {
            mHeart.setVisibility(View.GONE);
            mHeartOutline.setVisibility(View.VISIBLE);
        }
    }

    private void setReadInfo(GalleryDetail gd) {
        if (gd == null) {
            return;
        }

        ReadingRecord record = EhDB.getReadingRecord(gd.getId());
        if (record == null) {
            record = new ReadingRecord();
        }

        record.setId(gd.getId());
        record.setUpdate_time(gd.updateTime);
        record.setRead_time(gd.updateTime);
        record.setChapter_info(new Gson().toJson(gd.chapters));
        EhDB.putReadingRecord(record);
    }

    private void checkReadInfo(@NotNull GalleryDetail mGalleryDetail) {
        ReadingRecord record = EhDB.getReadingRecord(mGalleryDetail.getId());
        if (record != null && record.getChapter_info() != null) {
            GalleryChapterGroup[] chapters = new Gson().fromJson(record.getChapter_info(), GalleryChapterGroup[].class);
            for (GalleryChapterGroup group : chapters) {
                for (GalleryChapter chapter : group.getChapterList()) {
                    for (GalleryChapterGroup tgroup : mGalleryDetail.chapters) {
                        for (GalleryChapter tchapter : tgroup.getChapterList()) {
                            if (chapter.getTitle().equals(tchapter.getTitle())) {
                                tchapter.setRead(chapter.getRead());
                            }
                        }
                    }
                }
            }
        }
    }

    private void bindViewForDetail(GalleryDetail gd) {
        if (gd == null) {
            return;
        }
        if (mThumb == null || mTitle == null || mUploader == null || mCategory == null ||
                mLanguage == null || mPages == null || mSize == null || mPosted == null ||
                mFavoredTimes == null || mRatingText == null || mRating == null || mTorrent == null) {
            return;
        }

        Resources resources = getResources2();
        AssertUtils.assertNotNull(resources);

        mThumb.load(EhCacheKeyFactory.getThumbKey(gd.gid), gd.thumb);
        mTitle.setText(EhUtils.getSuitableTitle(gd));
        mUploader.setText(gd.uploader);
        mCategory.setText(EhUtils.getCategory(gd.category));
        mCategory.setTextColor(EhUtils.getCategoryColor(gd.category));
        updateFavoriteText();

        mPosted.setText(gd.posted);
        mFavoredTimes.setText(resources.getString(R.string.favored_times, gd.favoriteCount));

        mRatingText.setText(getAllRatingText(gd.rating, gd.ratingCount));
        mRating.setRating(gd.rating / 2.0f);

        updateFavoriteDrawable(gd);

        bindIntro(gd.intro);
        bindTags(gd.chapters);
        bindComments(gd.comments);
        // bindPreviews(gd);
    }

    private void bindSource(ViewGroup parent) {
        LayoutInflater inflater = getLayoutInflater2();
        if (inflater == null) {
            return;
        }

        for (MHApiSource source : MHApiSource.values()) {
            RadioButton item = (RadioButton) inflater.inflate(R.layout.item_source_bar, parent, false);
            item.setText(source.name().substring(0, 2));
            item.setTag(source);
            item.setId(View.generateViewId());
            parent.addView(item);
            if (source == currentSource) {
                item.setChecked(true);
            }
            item.setOnClickListener(v -> {
                MHApiSource target = (MHApiSource) v.getTag();
                MHApi.Companion.getINSTANCE().select(target);
                module.switchSource(target);
            });
        }
    }

    private void bindIntro(String intro) {
        Context context = getContext2();
        LayoutInflater inflater = getLayoutInflater2();
        if (null == context || null == inflater || null == mIntro || null == mIntroText) {
            return;
        }

        mIntroText.setText(intro);
    }

    @SuppressWarnings("deprecation")
    private void bindTags(GalleryChapterGroup[] tagGroups) {
        Context context = getContext2();
        LayoutInflater inflater = getLayoutInflater2();
        Resources resources = getResources2();
        if (null == context || null == inflater || null == resources || null == mTags || null == mNoTags) {
            return;
        }

        mTags.removeViews(1, mTags.getChildCount() - 1);
        if (tagGroups == null || tagGroups.length == 0) {
            mNoTags.setVisibility(View.VISIBLE);
            return;
        } else {
            mNoTags.setVisibility(View.GONE);
        }

        int colorTag = AttrResources.getAttrColor(context, R.attr.tagBackgroundColor);
        int colorName = AttrResources.getAttrColor(context, R.attr.tagGroupBackgroundColor);
        for (GalleryChapterGroup tg : tagGroups) {
            LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.gallery_tag_group, mTags, false);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            mTags.addView(ll);
            TextView tgName = (TextView) inflater.inflate(R.layout.item_gallery_tag, ll, false);
            ll.addView(tgName);
            tgName.setText(tg.getGroupName());
            tgName.setBackgroundDrawable(new RoundSideRectDrawable(colorName));

            AutoWrapLayout awl = new AutoWrapLayout(context);
            ll.addView(awl, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            for (int j = 0, z = tg.size(); j < z; j++) {
                TextView tag = (TextView) inflater.inflate(R.layout.item_gallery_tag, awl, false);
                awl.addView(tag);
                GalleryChapter chapter = tg.getChapterList().get(j);
                tag.setText(chapter.getTitle());
                tag.setBackgroundDrawable(new RoundSideRectDrawable(chapter.getRead() ? colorName : colorTag));
                tag.setTag(R.id.tag, chapter);
                tag.setOnClickListener(this);
            }
        }
    }

    private void bindComments(GalleryComment[] comments) {
        Context context = getContext2();
        LayoutInflater inflater = getLayoutInflater2();
        if (null == context || null == inflater || null == mComments || null == mCommentsText) {
            return;
        }

        mComments.removeViews(0, mComments.getChildCount() - 1);

        final int maxShowCount = 2;
        if (comments == null || comments.length == 0) {
            mCommentsText.setText(R.string.no_comments);
            return;
        } else if (comments.length <= maxShowCount) {
            mCommentsText.setText(R.string.no_more_comments);
        } else {
            mCommentsText.setText(R.string.more_comment);
        }

        int length = Math.min(maxShowCount, comments.length);
        for (int i = 0; i < length; i++) {
            GalleryComment comment = comments[i];
            View v = inflater.inflate(R.layout.item_gallery_comment, mComments, false);
            mComments.addView(v, i);
            TextView user = (TextView) v.findViewById(R.id.user);
            user.setText(comment.user);
            TextView time = (TextView) v.findViewById(R.id.time);
            time.setText(comment.time);
            ObservedTextView c = (ObservedTextView) v.findViewById(R.id.comment);
            c.setMaxLines(5);
            c.setText(Html.fromHtml(comment.comment,
                    new URLImageGetter(c, EhApplication.getConaco(context)), null));
        }
    }

    private String getAllRatingText(float rating, int ratingCount) {
        Resources resources = getResources2();
        AssertUtils.assertNotNull(resources);
        return resources.getString(R.string.rating_text, getRatingText(rating, resources), rating, ratingCount);
    }

    private void setTransitionName() {
        String gid = module.getGid();

        if (!gid.equals("-1") && mThumb != null &&
                mTitle != null && mUploader != null && mCategory != null) {
            ViewCompat.setTransitionName(mThumb, TransitionNameFactory.getThumbTransitionName(gid));
            ViewCompat.setTransitionName(mTitle, TransitionNameFactory.getTitleTransitionName(gid));
            ViewCompat.setTransitionName(mUploader, TransitionNameFactory.getUploaderTransitionName(gid));
            ViewCompat.setTransitionName(mCategory, TransitionNameFactory.getCategoryTransitionName(gid));
        }
    }

    private void ensurePopMenu() {
        if (mPopupMenu != null) {
            return;
        }

        Context context = getContext2();
        AssertUtils.assertNotNull(context);
        PopupMenu popup = new PopupMenu(context, mOtherActions, Gravity.TOP);
        mPopupMenu = popup;
        popup.getMenuInflater().inflate(R.menu.scene_gallery_detail, popup.getMenu());

        View.generateViewId();
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_open_in_other_app:
                    String url = module.getGalleryDetailUrl();
                    Activity activity = getActivity2();
                    if (null != url && null != activity) {
                        UrlOpener.openUrl(activity, url, false);
                    }
                    break;
                case R.id.action_refresh:
                    if (mState != STATE_REFRESH && mState != STATE_REFRESH_HEADER) {
                        adjustViewVisibility(STATE_REFRESH, true);
                        request();
                    }
                    break;
            }
            return true;
        });
    }

    private void showSimilarGalleryList(GalleryDetail gd) {
        if (null == gd) {
            return;
        }
        String keyword = EhUtils.extractTitle(gd.title);
        if (null != keyword) {
            ListUrlBuilder lub = new ListUrlBuilder();
            lub.setMode(ListUrlBuilder.MODE_NORMAL);
            lub.setKeyword(keyword);
            GalleryListScene.startScene(this, lub);
            return;
        }
        String artist = getArtist(gd.chapters);
        if (null != artist) {
            ListUrlBuilder lub = new ListUrlBuilder();
            lub.setMode(ListUrlBuilder.MODE_TAG);
            lub.setKeyword("artist:" + artist);
            GalleryListScene.startScene(this, lub);
            return;
        }
        if (null != gd.uploader) {
            ListUrlBuilder lub = new ListUrlBuilder();
            lub.setMode(ListUrlBuilder.MODE_UPLOADER);
            lub.setKeyword(gd.uploader);
            GalleryListScene.startScene(this, lub);
        }
    }

    private void showCoverGalleryList() {
        Context context = getContext2();
        if (null == context) {
            return;
        }
        String gid = module.getGid();
        if (gid.equals("-1")) {
            return;
        }
        File temp = AppConfig.createTempFile();
        if (null == temp) {
            return;
        }
        BeerBelly beerBelly = EhApplication.getConaco(context).getBeerBelly();

        OutputStream os = null;
        try {
            os = new FileOutputStream(temp);
            if (beerBelly.pullFromDiskCache(EhCacheKeyFactory.getThumbKey(gid), os)) {
                ListUrlBuilder lub = new ListUrlBuilder();
                lub.setMode(ListUrlBuilder.MODE_IMAGE_SEARCH);
                lub.setImagePath(temp.getPath());
                lub.setUseSimilarityScan(true);
                lub.setShowExpunged(true);
                GalleryListScene.startScene(this, lub);
            }
        } catch (FileNotFoundException e) {
            // Ignore
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    @Override
    public void onClick(View v) {
        Context context = getContext2();
        MainActivity activity = getActivity2();
        if (null == context || null == activity) {
            return;
        }

        if (mTip == v) {
            if (request()) {
                adjustViewVisibility(STATE_REFRESH, true);
            }
        } else if (mOtherActions == v) {
            ensurePopMenu();
            if (mPopupMenu != null) {
                mPopupMenu.show();
            }
        } else if (mFavorite == v) {
            GalleryInfo galleryInfo = module.getBaseInfo().getValue();
            if (galleryInfo == null) {
                return;
            }
            if (EhDB.containLocalFavorites(galleryInfo)) {
                EhDB.removeLocalFavorites(galleryInfo);
                showTip(R.string.remove_from_favorite_success, LENGTH_SHORT);
            } else {
                EhDB.putLocalFavorites(galleryInfo);
                showTip(R.string.add_to_favorite_success, LENGTH_SHORT);
            }
            updateFavoriteText();
        } else if (mRead == v) {

        } else if (mInfo == v) {
            Bundle args = new Bundle();
            args.putParcelable(GalleryInfoScene.KEY_GALLERY_DETAIL, module.getDetail().getValue());
            startScene(new Announcer(GalleryInfoScene.class).setArgs(args));
        } else if (mComments == v) {
            if (module.getDetail().getValue() == null) {
                return;
            }

            Bundle args = new Bundle();
            args.putString(GalleryCommentsScene.KEY_GID, module.getDetail().getValue().gid);
            args.putParcelableArray(GalleryCommentsScene.KEY_COMMENTS, module.getDetail().getValue().comments);
            startScene(new Announcer(GalleryCommentsScene.class)
                    .setArgs(args)
                    .setRequestCode(this, REQUEST_CODE_COMMENT_GALLERY));
        } else {
            if (module.getDetail().getValue() == null) {
                return;
            }

            GalleryChapter c = (GalleryChapter) v.getTag(R.id.tag);
            c.setRead(true);
            setReadInfo(module.getDetail().getValue());

            module.getDetail().getValue().cid = c.getUrl();
            Intent intent = new Intent(activity, GalleryActivity.class);
            intent.setAction(GalleryActivity.ACTION_EH);
            intent.putExtra(GalleryActivity.KEY_GALLERY_INFO, module.getDetail().getValue());
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewTransition != null && mThumb != null &&
                mViewTransition.getShownViewIndex() == 0 && mThumb.isShown()) {
            int[] location = new int[2];
            mThumb.getLocationInWindow(location);
            // Only show transaction when thumb can be seen
            if (location[1] + mThumb.getHeight() > 0) {
                setTransitionName();
                finish(new ExitTransaction(mThumb));
                return;
            }
        }
        finish();
    }


    private void updateFavoriteText() {
        if (null == mFavorite) {
            return;
        }

        if (module.getBaseInfo().getValue() == null) {
            mFavorite.setText(R.string.add_to_favourites);
            return;
        }

        boolean favourite = EhDB.containLocalFavorites(module.getBaseInfo().getValue());
        mFavorite.setText(favourite ? R.string.remove_from_favourites : R.string.add_to_favourites);
    }


    private void onGetGalleryDetailFailure(Exception e) {
        e.printStackTrace();
        Context context = getContext2();
        if (null != context && null != mTip) {
            String error = ExceptionUtils.getReadableString(e);
            mTip.setText(error);
            adjustViewVisibility(STATE_FAILED, true);
        }
    }


    @IntDef({STATE_INIT, STATE_NORMAL, STATE_REFRESH, STATE_REFRESH_HEADER, STATE_FAILED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    private static class ExitTransaction implements TransitionHelper {

        private final View mThumb;

        public ExitTransaction(View thumb) {
            mThumb = thumb;
        }

        @Override
        public boolean onTransition(Context context,
                                    FragmentTransaction transaction, Fragment exit, Fragment enter) {
            if (!(enter instanceof GalleryListScene) && !(enter instanceof DownloadsScene) &&
                    !(enter instanceof FavoritesScene) && !(enter instanceof HistoryScene)) {
                return false;
            }

            String transitionName = ViewCompat.getTransitionName(mThumb);
            if (transitionName != null) {
                exit.setSharedElementReturnTransition(
                        TransitionInflater.from(context).inflateTransition(R.transition.trans_move));
                exit.setExitTransition(
                        TransitionInflater.from(context).inflateTransition(R.transition.trans_fade));
                enter.setSharedElementEnterTransition(
                        TransitionInflater.from(context).inflateTransition(R.transition.trans_move));
                enter.setEnterTransition(
                        TransitionInflater.from(context).inflateTransition(R.transition.trans_fade));
                transaction.addSharedElement(mThumb, transitionName);
            }
            return true;
        }
    }

    @Override
    public void loadSource() {
    }
}
