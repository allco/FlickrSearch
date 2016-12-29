package com.allco.flickrsearch.photolist;

import android.content.Context;

import com.allco.flickrsearch.photolist.view.PhotoListAdapter;
import com.allco.flickrsearch.photolist.view.PhotoListFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
public class PhotoListPresenterTest {

    @Mock
    Context context;

    @Mock
    PhotoListAdapter photoListAdapter;

    @Mock
    PhotoListModel photoListModel;

    private PhotoListPresenter photoListPresenter;

    @Before
    public void setUp() throws Exception {
        photoListPresenter = new PhotoListPresenter(context, photoListAdapter, photoListModel);
    }

    @Test
    public void init() throws Exception {
        PhotoListFragment photoListFragment = mock(PhotoListFragment.class);
        photoListPresenter.init(photoListFragment);
    }

    @Test
    public void onResume() throws Exception {

    }

    @Test
    public void destroy() throws Exception {

    }

    @Test
    public void getSearchRequest() throws Exception {

    }

    @Test
    public void inflateMenu() throws Exception {

    }
}