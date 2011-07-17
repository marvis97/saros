package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.CANCEL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ERROR_MESSAGE_COULD_NOT_CONNECT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ERROR_MESSAGE_PASSWORDS_NOT_MATCH;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.FINISH;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.LABEL_PASSWORD;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.LABEL_REPEAT_PASSWORD;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.LABEL_USER_NAME;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.LABEL_XMPP_JABBER_SERVER;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_CREATE_ACCOUNT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_SAROS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_CREATE_XMPP_JABBER_ACCOUNT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class SarosPreferencesTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @After
    public void afterEveryTest() throws RemoteException {
        resetDefaultAccount();
    }

    @Test(expected = RuntimeException.class)
    public void createExistedAccountWithMenuSarosCreateAccount()
        throws RemoteException {
        ALICE.superBot().menuBar().saros()
            .createAccount(ALICE.getJID(), Constants.PASSWORD);
    }

    /*
     * NOTE: createAccount can not be repeated tested. The reasons are:
     * 
     * 1.registered user can not be automatically deleted.
     * 
     * 2. It's not allowed to register account so fast.
     */
    @Test
    @Ignore
    public void createAccountWithButtonAddAccountInShellSarosPeferences()
        throws RemoteException {
        ALICE.superBot().menuBar().saros().preferences()
            .createAccount(Constants.JID_TO_CREATE, Constants.PASSWORD);
    }

    @Test
    public void createAccountWhichAlreadyExisted() throws RemoteException {
        ALICE.remoteBot().menu(MENU_SAROS).menu(MENU_CREATE_ACCOUNT).click();

        ALICE.remoteBot()
            .waitUntilShellIsOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        IRemoteBotShell shell = ALICE.remoteBot().shell(
            SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        shell.activate();
        shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_SERVER)
            .setText(Constants.SERVER);
        shell.bot().textWithLabel(LABEL_USER_NAME)
            .setText(ALICE.getJID().getName());
        shell.bot().textWithLabel(LABEL_PASSWORD).setText(ALICE.getPassword());
        shell.bot().textWithLabel(LABEL_REPEAT_PASSWORD)
            .setText(ALICE.getPassword());
        shell.bot().sleep(1000);
        assertFalse("could create a duplicated account",
            shell.bot().button(FINISH).isEnabled());
        assertEquals(ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS,
            shell.getErrorMessage());
        shell.bot().button(CANCEL).click();
        shell.waitShortUntilIsClosed();
    }

    /**
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore("there are bugs: can't correctly check if the given passwords are same or not")
    public void createAccountWithDismatchedPassword() throws RemoteException {
        ALICE.remoteBot().menu(MENU_SAROS).menu(MENU_CREATE_ACCOUNT).click();

        ALICE.remoteBot()
            .waitUntilShellIsOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        IRemoteBotShell shell = ALICE.remoteBot().shell(
            SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        shell.activate();
        shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_SERVER)
            .setText(Constants.SERVER);
        shell.bot().textWithLabel(LABEL_USER_NAME)
            .setText(Constants.NEW_XMPP_JABBER_ID);
        shell.bot().textWithLabel(LABEL_PASSWORD).setText(Constants.PASSWORD);
        shell.bot().textWithLabel(LABEL_REPEAT_PASSWORD)
            .setText(Constants.NO_MATCHED_REPEAT_PASSWORD);

        assertFalse(shell.bot().button(FINISH).isEnabled());
        String errorMessage = shell.getErrorMessage();
        assertTrue(errorMessage.equals(ERROR_MESSAGE_PASSWORDS_NOT_MATCH));
        shell.confirm(CANCEL);
        assertFalse(ALICE.remoteBot().isShellOpen(
            SHELL_CREATE_XMPP_JABBER_ACCOUNT));
    }

    /**
     * FIXME: by fist run you will get the error message
     * {@link StfRemoteObject#ERROR_MESSAGE_NOT_CONNECTED_TO_SERVER}, but by
     * second run you will get anther error message
     * {@link StfRemoteObject#ERROR_MESSAGE_COULD_NOT_CONNECT}
     * 
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void createAccountWithInvalidServer() throws RemoteException {
        ALICE.remoteBot().menu(MENU_SAROS).menu(MENU_CREATE_ACCOUNT).click();
        ALICE.remoteBot()
            .waitUntilShellIsOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        IRemoteBotShell shell_ALICE = ALICE.remoteBot().shell(
            SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        shell_ALICE.activate();

        Map<String, String> labelsAndTexts = new HashMap<String, String>();
        labelsAndTexts.put(LABEL_XMPP_JABBER_SERVER,
            Constants.INVALID_SERVER_NAME);
        labelsAndTexts.put(LABEL_USER_NAME, Constants.NEW_XMPP_JABBER_ID);
        labelsAndTexts.put(LABEL_PASSWORD, Constants.PASSWORD);
        labelsAndTexts.put(LABEL_REPEAT_PASSWORD, Constants.PASSWORD);

        shell_ALICE.confirmWithTextFieldAndWait(labelsAndTexts, FINISH);

        shell_ALICE.bot().button(FINISH).waitLongUntilIsEnabled();

        String errorMessage = shell_ALICE.getErrorMessage();
        assertTrue(errorMessage.matches(ERROR_MESSAGE_COULD_NOT_CONNECT));
        shell_ALICE.confirm(CANCEL);
        assertFalse(ALICE.remoteBot().isShellOpen(
            SHELL_CREATE_XMPP_JABBER_ACCOUNT));
    }

    @Test
    public void addAndActivateAcount() throws RemoteException {
        assertFalse(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(Constants.JID_TO_ADD));
        ALICE.superBot().menuBar().saros().preferences()
            .addAccount(Constants.JID_TO_ADD, Constants.PASSWORD);
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(Constants.JID_TO_ADD));
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .isAccountActive(ALICE.getJID()));
        assertFalse(ALICE.superBot().menuBar().saros().preferences()
            .isAccountActive(Constants.JID_TO_ADD));
        ALICE.superBot().menuBar().saros().preferences()
            .activateAccount(Constants.JID_TO_ADD);
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .isAccountActive(Constants.JID_TO_ADD));
        assertFalse(ALICE.superBot().menuBar().saros().preferences()
            .isAccountActive(ALICE.getJID()));
    }

    @Test
    public void editAccount() throws RemoteException {
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(ALICE.getJID()));
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .isAccountActive(ALICE.getJID()));
        ALICE
            .superBot()
            .menuBar()
            .saros()
            .preferences()
            .changeAccount(ALICE.getJID(), Constants.NEW_XMPP_JABBER_ID,
                Constants.PASSWORD);
        assertFalse(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(ALICE.getJID()));
        assertFalse(ALICE.superBot().menuBar().saros().preferences()
            .isAccountActive(ALICE.getJID()));
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(Constants.JID_TO_CHANGE));
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .isAccountActive(Constants.JID_TO_CHANGE));
    }

    @Test(expected = RuntimeException.class)
    @Ignore
    public void deleteActiveAccount() throws RemoteException {
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(ALICE.getJID()));
        ALICE.superBot().menuBar().saros().preferences()
            .deleteAccount(ALICE.getJID(), ALICE.getPassword());
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .isAccountActive(ALICE.getJID()));
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(ALICE.getJID()));
    }

    @Test
    public void deleteInactiveAccount() throws RemoteException {
        assertFalse(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(Constants.JID_TO_ADD));
        ALICE.superBot().menuBar().saros().preferences()
            .addAccount(Constants.JID_TO_ADD, Constants.PASSWORD);
        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(Constants.JID_TO_ADD));
        ALICE.superBot().menuBar().saros().preferences()
            .deleteAccount(Constants.JID_TO_ADD, Constants.PASSWORD);
        assertFalse(ALICE.superBot().menuBar().saros().preferences()
            .existsAccount(Constants.JID_TO_ADD));
    }
}
