package com.allco.flickrsearch.photolist;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.ioc.ApplicationComponent;
import com.allco.flickrsearch.ioc.ApplicationModule;
import com.allco.flickrsearch.ioc.IoC;
import com.allco.flickrsearch.photodetails.PhotoDetailsActivity;
import com.allco.flickrsearch.photolist.view.PhotoListAdapter;
import com.allco.flickrsearch.photolist.view.PhotoListFragment;
import com.allco.flickrsearch.photolist.view.PhotoListItemData;
import com.allco.flickrsearch.rest.FlickrItemData;
import com.flextrade.jfixture.JFixture;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.List;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import it.cosenonjaviste.daggermock.InjectFromComponent;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("WeakerAccess")
@RunWith(PowerMockRunner.class)
public class PhotoListPresenterTest {

    @Rule
    public MethodRule mockitoRule;

    @Mock
    PhotoListFragment photoListFragment;

    @Mock
    PhotoListAdapter photoListAdapter;

    @Mock
    PhotoListModel photoListModel;

    @Mock
    PhotoListPresenter.Listener presenterListener;

    @InjectFromComponent(PhotoListFragment.class)
    PhotoListPresenter photoListPresenter;

    JFixture jFixture = new JFixture();
    Context context = mock(Context.class);
    Resources resources = mock(Resources.class);
    String searchRequest = jFixture.create(String.class);

    public PhotoListPresenterTest() throws Exception {
        doReturn(resources).when(context).getResources();
        mockitoRule = new DaggerMockRule<>(ApplicationComponent.class, new ApplicationModule(mock(Context.class)))
                .set(applicationComponent -> IoC.getInstance().setApplicationComponent(applicationComponent));
    }

    @Before
    public void setUp() throws Exception {
        photoListPresenter = spy(photoListPresenter);
    }

    @Test
    public void testAttachAdapterToListView() throws Exception {
        ListView listView = mock(ListView.class);

        photoListPresenter.attach(listView);
        verify(listView).setOnScrollListener(any(AbsListView.OnScrollListener.class));
        verify(listView).setOnItemClickListener(any(AdapterView.OnItemClickListener.class));
        verify(listView).setAdapter(any(ListAdapter.class));
    }

    @Test
    @PrepareForTest(PhotoDetailsActivity.class)
    public void testCreateOnItemClickListener() throws Exception {
        int position = jFixture.create(Integer.class);
        PhotoListItemData itemData = jFixture.create(PhotoListItemData.class);
        doReturn(itemData).when(photoListAdapter).getItem(anyInt());

        Intent intentStartDetails = mock(Intent.class);
        PowerMockito.mockStatic(PhotoDetailsActivity.class);
        when(PhotoDetailsActivity.createIntent(any(Context.class), anyString(), anyString())).thenReturn(intentStartDetails);

        AdapterView.OnItemClickListener onItemClickListener = photoListPresenter.createOnItemClickListener();
        assertNotNull(onItemClickListener);

        onItemClickListener.onItemClick(null, null, position, 0);
        verify(photoListAdapter).getItem(eq(position));
        verify(context).startActivity(same(intentStartDetails));

        PowerMockito.verifyStatic();
        PhotoDetailsActivity.createIntent(same(context), eq(itemData.getTitle()), eq(itemData.getImageUrl()));
    }

    @Test
    @PrepareForTest(TextUtils.class)
    public void testStartRequestingSearchRequestEmpty() throws Exception {
        PowerMockito.mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(any(CharSequence.class))).thenReturn(true);
        Whitebox.setInternalState(photoListPresenter, "searchRequest", "");
        doNothing().when(photoListPresenter).showMessage(anyInt(), anyBoolean());

        photoListPresenter.startRequesting();
        verify(photoListPresenter).showMessage(eq(R.string.please_enter_text_search), eq(false));
    }

    @Test
    @PrepareForTest(TextUtils.class)
    public void testStartRequestingSearchRequestValid() throws Exception {
        PowerMockito.mockStatic(TextUtils.class);
        when(TextUtils.isEmpty(any(CharSequence.class))).thenReturn(false);
        doNothing().when(photoListPresenter).refresh(anyBoolean());

        photoListPresenter.startRequesting();
        verify(photoListPresenter).refresh(eq(true));
    }

    @Test
    public void testDestroy() throws Exception {
        photoListPresenter.destroy();
        verify(photoListModel).destroy();
    }

    @Test
    public void testOnResume() throws Exception {
        photoListPresenter.onResume();
        verify(presenterListener).onRequestChanged(eq(searchRequest));
    }

    @Test
    public void testShowMessage() throws Exception {
        int resId = jFixture.create(Integer.class);
        boolean showRefresh = jFixture.create(Boolean.class);
        String text = jFixture.create(String.class);
        doReturn(text).when(context).getString(eq(resId));
        MenuItem menuItemRefresh = mock(MenuItem.class);
        doReturn(!showRefresh).when(menuItemRefresh).isVisible();
        Whitebox.setInternalState(photoListPresenter, "menuItemRefresh", menuItemRefresh);
        photoListPresenter.showMessage(resId, showRefresh);

        verify(menuItemRefresh).setVisible(eq(showRefresh));
        verify(context).getString(eq(resId));
        verify(photoListFragment).setEmptyText(eq(text));
        verify(photoListFragment).setListShownNoAnimation(eq(true));
    }

    @Test
    public void testInflateMenu() throws Exception {
        Menu menu = mock(Menu.class);
        MenuItem menuItem = mock(MenuItem.class);
        MenuInflater menuInflater = mock(MenuInflater.class);
        doReturn(menuItem).when(menu).findItem(eq(R.id.action_refresh));
        doNothing().when(photoListPresenter).refresh(anyBoolean());

        final MenuItem.OnMenuItemClickListener[] menuItemClickListener = new MenuItem.OnMenuItemClickListener[1];
        doAnswer(invocation -> {
            menuItemClickListener[0] = invocation.getArgumentAt(0, MenuItem.OnMenuItemClickListener.class);
            return null;
        }).when(menuItem).setOnMenuItemClickListener(any());

        photoListPresenter.inflateMenu(menu, menuInflater);
        verify(menuInflater).inflate(eq(R.menu.menu_photolist), same(menu));
        verify(menu).findItem(eq(R.id.action_refresh));
        assertNotNull(menuItemClickListener[0]);
        // emulate menu item click
        assertTrue(menuItemClickListener[0].onMenuItemClick(menuItem));
        verify(photoListPresenter).refresh(eq(false));
    }

    @Test
    public void testRefresh() {
        boolean allowCache = jFixture.create(Boolean.class);
        Runnable nextPageLoader = mock(Runnable.class);
        doNothing().when(photoListPresenter).onResume();
        doReturn(nextPageLoader).when(photoListPresenter).createNextPageLoader();

        final PhotoListAdapter.GetMorePagesAvailable[] getMorePagesAvailable = new PhotoListAdapter.GetMorePagesAvailable[1];
        doAnswer(inv -> {
            getMorePagesAvailable[0] = inv.getArgumentAt(0, PhotoListAdapter.GetMorePagesAvailable.class);
            return null;
        }).when(photoListAdapter).reset(any(), any());

        photoListPresenter.refresh(allowCache);
        verify(photoListPresenter).onResume();
        verify(photoListModel).reset(eq(searchRequest), eq(allowCache));
        verify(photoListAdapter).reset(any(PhotoListAdapter.GetMorePagesAvailable.class), same(nextPageLoader));
        verify(photoListFragment).setListShown(eq(false));
        verify(nextPageLoader).run();

        assertNotNull(getMorePagesAvailable[0]);
        boolean isFinished = jFixture.create(Boolean.class);
        doReturn(isFinished).when(photoListModel).isFinished();
        assertEquals(!isFinished, getMorePagesAvailable[0].get());
        verify(photoListModel).isFinished();
    }

    @Test
    public void testCreateNextPageLoader() {
        Runnable nextPageLoader = photoListPresenter.createNextPageLoader();
        assertNotNull(nextPageLoader);
        final PhotoListModel.OnDataErrorListener[] onDataErrorListener = new PhotoListModel.OnDataErrorListener[1];
        final PhotoListModel.OnDataAvailableListener[] onDataAvailableListener = new PhotoListModel.OnDataAvailableListener[1];
        doAnswer(inv -> {
            onDataAvailableListener[0] = inv.getArgumentAt(1, PhotoListModel.OnDataAvailableListener.class);
            onDataErrorListener[0] = inv.getArgumentAt(2, PhotoListModel.OnDataErrorListener.class);
            return null;
        }).when(photoListModel).tryLoadNextPage(eq(false), any(), any());

        nextPageLoader.run();
        verify(photoListModel).tryLoadNextPage(eq(false), any(PhotoListModel.OnDataAvailableListener.class), any(PhotoListModel.OnDataErrorListener.class));

        assertNotNull(onDataErrorListener);
        assertNotNull(onDataAvailableListener);
        doNothing().when(photoListPresenter).onPageLoaded(any());
        doNothing().when(photoListPresenter).showMessage(anyInt(), anyBoolean());
        onDataErrorListener[0].onDataError();
        verify(photoListPresenter).showMessage(eq(R.string.error_occurred), eq(true));
        onDataAvailableListener[0].onDataAvailable(mock(List.class));
        verify(photoListPresenter).onPageLoaded(any(List.class));
    }

    @Test
    @PrepareForTest(PhotoListItemData.class)
    public void testOnPageLoadedEntriesValid() {
        List<FlickrItemData.Entry> entries = jFixture.create(new TypeToken<List<FlickrItemData.Entry>>() {}.getType());
        List<PhotoListItemData> photoListItemData = jFixture.create(new TypeToken<List<PhotoListItemData>>() {}.getType());
        PowerMockito.mockStatic(PhotoListItemData.class);
        when(PhotoListItemData.map(eq(entries))).thenReturn(photoListItemData);
        MenuItem menuItemRefresh = mock(MenuItem.class);
        Whitebox.setInternalState(photoListPresenter, "menuItemRefresh", menuItemRefresh);

        photoListPresenter.onPageLoaded(entries);
        PowerMockito.verifyStatic();
        PhotoListItemData.map(eq(entries));
        verify(photoListAdapter).addData(eq(photoListItemData));
        verify(menuItemRefresh).setVisible(eq(true));
        verify(photoListFragment).setListShownNoAnimation(eq(true));
    }

    @Test
    public void testOnPageLoadedEntriesEmpty() {
        doReturn(true).when(photoListModel).isFinished();
        doReturn(0).when(photoListAdapter).getCount();
        doNothing().when(photoListPresenter).showMessage(anyInt(), anyBoolean());

        photoListPresenter.onPageLoaded(null);
        verify(photoListPresenter).showMessage(eq(R.string.nothing_found_try_other_request), eq(false));
    }
}