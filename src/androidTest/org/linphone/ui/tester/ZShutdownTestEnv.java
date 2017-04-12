package org.linphone.ui.tester;

import static android.content.Intent.ACTION_MAIN;

import com.trsmicloud.LinphoneService;

import android.content.Intent;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Sylvain Berfini
 */
public class ZShutdownTestEnv extends SampleTest {

	@SmallTest
	@MediumTest
	@LargeTest
	public void testZShutDownLinphoneCore() {
		LinphoneTestManager.destroy();
		aContext.stopService(new Intent(ACTION_MAIN).setClass(aContext, LinphoneService.class));
	}

}
