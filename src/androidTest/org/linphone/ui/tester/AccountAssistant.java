package org.linphone.ui.tester;

import junit.framework.Assert;

import com.trsmicloud.LinphoneActivity;
import com.trsmicloud.LinphoneManager;
import com.trsmicloud.LinphonePreferences;
import com.trsmicloud.assistant.AssistantActivity;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.video.capture.hwconf.Hacks;

import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

/**
 * @author Sylvain Berfini
 */
public class AccountAssistant extends SampleTest {

	@SmallTest
	@MediumTest
	@LargeTest
	public void testAWizardDisplayedAfterInstall() {
		LinphonePreferences.instance().setXmlrpcUrl("https://sip3.linphone.org:444/inapp.php");
		solo.waitForActivity("AssistantActivity", 3000);
		solo.assertCurrentActivity("Expected Assistant Activity", AssistantActivity.class);
	}

	@SmallTest
	@MediumTest
	@LargeTest
	public void testBLoginWithLinphoneAccount() {
		solo.waitForActivity("AssistantActivity", 3000);
		solo.assertCurrentActivity("Expected Assistant Activity", AssistantActivity.class);

		solo.clickOnView(solo.getView(com.trsmicloud.R.id.login_linphone));
		solo.clickOnView(solo.getView(com.trsmicloud.R.id.use_username));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.assistant_username), iContext.getString(com.trsmicloud.R.string.account_linphone_login));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.assistant_password), iContext.getString(com.trsmicloud.R.string.account_linphone_pwd));
		solo.clickOnView(solo.getView(com.trsmicloud.R.id.assistant_apply));

		solo.clickOnView(solo.getView(com.trsmicloud.R.id.assistant_skip));

		solo.sleep(1000);

		//Test echo calibration launch at first start
		Assert.assertTrue(solo.searchText(aContext.getString(com.trsmicloud.R.string.assistant_codec_down_question)));
		solo.clickOnView(solo.getView(com.trsmicloud.R.id.answerNo));

		solo.waitForActivity("LinphoneActivity", 8000);
		Assert.assertTrue(solo.searchText(iContext.getString(com.trsmicloud.R.string.account_linphone_login) + "@sip.linphone.org"));

		solo.sleep(3000); //Wait for registration to be done
		LinphoneProxyConfig[] proxyConfigs = LinphoneManager.getLc().getProxyConfigList();
		Assert.assertEquals(1, proxyConfigs.length);
		LinphoneProxyConfig proxyConfig = proxyConfigs[0];
		waitForRegistration(proxyConfig);

		//Check the wizard added sip.linphone.org custom settings
		LinphonePreferences prefs = LinphonePreferences.instance();
		String stunServer = prefs.getStunServer();
		Assert.assertEquals(aContext.getString(com.trsmicloud.R.string.default_stun), stunServer);

		String transport = prefs.getAccountTransportKey(0);
		Assert.assertEquals(aContext.getString(com.trsmicloud.R.string.pref_transport_tls_key), transport);

		String proxy = prefs.getAccountProxy(0);
		Assert.assertEquals("<sip:" + aContext.getString(com.trsmicloud.R.string.default_domain) + ";transport=tls>", proxy);

		String username = prefs.getAccountUsername(0);
		Assert.assertEquals(iContext.getString(com.trsmicloud.R.string.account_linphone_login), username);

		boolean ice = prefs.isIceEnabled();
		Assert.assertEquals(ice, true);
	}

	@LargeTest
	public void testCWizardDoesntShowWhenAccountIsConfigured() {
		solo.waitForActivity("LinphoneActivity", 2000);
		solo.assertCurrentActivity("Expected Linphone Activity", LinphoneActivity.class);
	}

	@LargeTest
	public void testDLoginWithGenericAccount() {
		startAssistant();

		solo.clickOnView(solo.getView(com.trsmicloud.R.id.login_generic));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.assistant_username), iContext.getString(com.trsmicloud.R.string.account_generic_login));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.assistant_password), iContext.getString(com.trsmicloud.R.string.account_generic_pwd));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.assistant_domain), iContext.getString(com.trsmicloud.R.string.account_generic_domain));
		solo.clickOnView(solo.getView(com.trsmicloud.R.id.assistant_apply));

		if (!Hacks.hasBuiltInEchoCanceller())
			solo.waitForActivity("LinphoneActivity", 8000);
		else
			solo.waitForActivity("LinphoneActivity", 2000);
		Assert.assertTrue(solo.searchText(iContext.getString(com.trsmicloud.R.string.account_generic_login) + "@" + iContext.getString(com.trsmicloud.R.string.account_generic_domain)));

		solo.sleep(3000); //Wait for registration to be done
		LinphoneProxyConfig[] proxyConfigs = LinphoneManager.getLc().getProxyConfigList();
		Assert.assertEquals(proxyConfigs.length, 2);
		LinphoneProxyConfig proxyConfig = proxyConfigs[1];
		waitForRegistration(proxyConfig);
	}

	@LargeTest
	public void testECreateNewAccount() {
		startAssistant();

		solo.clickOnView(solo.getView(com.trsmicloud.R.id.create_account));

		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.username), iContext.getString(com.trsmicloud.R.string.account_create_login).substring(0, 2));
		solo.sleep(200);
		TextView error = (TextView) solo.getView(com.trsmicloud.R.id.username_error);
		int sleepingTime = 1500;
		Button createAccount = (Button) solo.getView(com.trsmicloud.R.id.assistant_create);

		Assert.assertEquals(error.getText(), aContext.getString(com.trsmicloud.R.string.wizard_username_incorrect));
		Assert.assertFalse(createAccount.isEnabled());

		solo.clearEditText((EditText) solo.getView(com.trsmicloud.R.id.username));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.username), iContext.getString(com.trsmicloud.R.string.account_linphone_login));
		solo.sleep(sleepingTime * 2);
		Assert.assertEquals(error.getText(), aContext.getString(com.trsmicloud.R.string.wizard_username_unavailable));
		Assert.assertFalse(createAccount.isEnabled());

		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.password), iContext.getString(com.trsmicloud.R.string.account_create_pwd).substring(0, 2));
		solo.sleep(sleepingTime);
		error = (TextView) solo.getView(com.trsmicloud.R.id.confirm_password_error);
		Assert.assertEquals(error.getText(), aContext.getString(com.trsmicloud.R.string.wizard_passwords_unmatched));
		Assert.assertFalse(createAccount.isEnabled());

		solo.clearEditText((EditText) solo.getView(com.trsmicloud.R.id.password));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.password), iContext.getString(com.trsmicloud.R.string.account_create_pwd).substring(0, 2));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.confirm_password), iContext.getString(com.trsmicloud.R.string.account_create_pwd).substring(0,2));
		solo.sleep(sleepingTime);
		error = (TextView) solo.getView(com.trsmicloud.R.id.password_error);
		Assert.assertEquals(error.getText(), aContext.getString(com.trsmicloud.R.string.wizard_password_incorrect));
		Assert.assertFalse(createAccount.isEnabled());

		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.email), iContext.getString(com.trsmicloud.R.string.account_create_email).substring(0, 12));
		solo.sleep(sleepingTime);
		error = (TextView) solo.getView(com.trsmicloud.R.id.email_error);
		Assert.assertEquals(error.getText(), aContext.getString(com.trsmicloud.R.string.wizard_email_incorrect));
		Assert.assertFalse(createAccount.isEnabled());

		solo.clearEditText((EditText) solo.getView(com.trsmicloud.R.id.username));
		solo.clearEditText((EditText) solo.getView(com.trsmicloud.R.id.password));
		solo.clearEditText((EditText) solo.getView(com.trsmicloud.R.id.confirm_password));
		solo.clearEditText((EditText) solo.getView(com.trsmicloud.R.id.email));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.username), iContext.getString(com.trsmicloud.R.string.account_create_login));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.password), iContext.getString(com.trsmicloud.R.string.account_create_pwd));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.confirm_password), iContext.getString(com.trsmicloud.R.string.account_create_pwd));
		solo.enterText((EditText) solo.getView(com.trsmicloud.R.id.email), iContext.getString(com.trsmicloud.R.string.account_create_email));
		solo.sleep(sleepingTime);
		Assert.assertEquals(error.getText(), "");
		Assert.assertTrue(createAccount.isEnabled());
	}

	@LargeTest
	public void testFCancelWizard() {
		startAssistant();
		solo.clickOnView(solo.getView(com.trsmicloud.R.id.assistant_cancel));

		solo.waitForActivity("LinphoneActivity", 2000);
		solo.assertCurrentActivity("Expected Linphone Activity", LinphoneActivity.class);
	}

	private void startAssistant() {
		solo.waitForActivity("LinphoneActivity", 2000);
		solo.assertCurrentActivity("Expected Linphone Activity", LinphoneActivity.class);

		solo.clickOnView(solo.getView(com.trsmicloud.R.id.side_menu_button));
		solo.clickOnText(aContext.getString(com.trsmicloud.R.string.menu_assistant));
	}
}
