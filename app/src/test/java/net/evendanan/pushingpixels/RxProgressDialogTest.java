package net.evendanan.pushingpixels;

import static org.mockito.ArgumentMatchers.anyInt;

import android.view.View;
import android.widget.TextView;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;

import io.reactivex.disposables.Disposable;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RxProgressDialogTest {

    @Test
    public void testLifecycle() throws Exception {
        ActivityController<MainSettingsActivity> controller = Robolectric.buildActivity(MainSettingsActivity.class);
        controller.setup();

        Data data = Mockito.mock(Data.class);
        final Data errorData = Mockito.mock(Data.class);

        Assert.assertNull(ShadowDialog.getLatestDialog());

        final Disposable disposable = RxProgressDialog.create(data, controller.get())
                .map(d -> {
                    d.call(1);
                    Assert.assertNotNull(ShadowDialog.getLatestDialog());
                    Assert.assertTrue(ShadowDialog.getLatestDialog().isShowing());
                    final TextView messageView = ShadowDialog.getLatestDialog().findViewById(R.id.progress_message);
                    Assert.assertNotNull(messageView);
                    Assert.assertEquals(View.GONE, messageView.getVisibility());
                    return d;
                })
                .subscribe(d -> {
                    d.call(2);
                    Assert.assertNotNull(ShadowDialog.getLatestDialog());
                    Assert.assertTrue(ShadowDialog.getLatestDialog().isShowing());
                }, throwable -> errorData.call(0));

        Mockito.verifyZeroInteractions(errorData);

        final InOrder inOrder = Mockito.inOrder(data);
        inOrder.verify(data).call(1);
        inOrder.verify(data).call(2);
        inOrder.verifyNoMoreInteractions();

        Assert.assertNotNull(ShadowDialog.getLatestDialog());
        Assert.assertFalse(ShadowDialog.getLatestDialog().isShowing());

        disposable.dispose();

        Assert.assertNotNull(ShadowDialog.getLatestDialog());
        Assert.assertFalse(ShadowDialog.getLatestDialog().isShowing());
    }

    @Test
    public void testShowMessage() throws Exception {
        ActivityController<MainSettingsActivity> controller = Robolectric.buildActivity(MainSettingsActivity.class);
        controller.setup();

        Data data = Mockito.mock(Data.class);

        Assert.assertNull(ShadowDialog.getLatestDialog());

        final Disposable disposable = RxProgressDialog.create(data, controller.get(), "this is a message")
                .map(d -> {
                    final TextView messageView = ShadowDialog.getLatestDialog().findViewById(R.id.progress_message);
                    Assert.assertNotNull(messageView);
                    Assert.assertEquals("this is a message", messageView.getText());
                    Assert.assertEquals(View.VISIBLE, messageView.getVisibility());
                    return d;
                })
                .subscribe(d -> { }, throwable -> { });

        disposable.dispose();

        Assert.assertNotNull(ShadowDialog.getLatestDialog());
        Assert.assertFalse(ShadowDialog.getLatestDialog().isShowing());
    }

    @Test
    public void testLifecycleWithError() throws Exception {
        ActivityController<MainSettingsActivity> controller = Robolectric.buildActivity(MainSettingsActivity.class);
        controller.setup();

        Data data = Mockito.mock(Data.class);
        Mockito.doThrow(new RuntimeException()).when(data).call(anyInt());

        final Data errorData = Mockito.mock(Data.class);

        Assert.assertNull(ShadowDialog.getLatestDialog());

        final Disposable disposable = RxProgressDialog.create(data, controller.get())
                .map(d -> {
                    Assert.assertNotNull(ShadowDialog.getLatestDialog());
                    Assert.assertTrue(ShadowDialog.getLatestDialog().isShowing());
                    d.call(1);
                    return d;
                })
                .subscribe(d -> d.call(2), throwable -> errorData.call(0));

        Mockito.verify(errorData).call(0);
        Mockito.verifyNoMoreInteractions(errorData);

        Mockito.verify(data).call(1);
        Mockito.verifyNoMoreInteractions(data);

        Assert.assertNotNull(ShadowDialog.getLatestDialog());
        Assert.assertFalse(ShadowDialog.getLatestDialog().isShowing());

        disposable.dispose();

        Assert.assertNotNull(ShadowDialog.getLatestDialog());
        Assert.assertFalse(ShadowDialog.getLatestDialog().isShowing());
    }

    interface Data {
        void call(int value);
    }
}