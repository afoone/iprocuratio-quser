/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsys.quser;

import java.util.LinkedList;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import static ru.apertum.qsystem.client.forms.FClient.*;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.cmd.CmdParams;
import ru.apertum.qsystem.common.cmd.RpcInviteCustomer;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.server.controller.Executer;
import ru.apertum.qsystem.server.model.QPlanService;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;
import ru.apertum.qsystem.server.model.postponed.QPostponedList;
import ru.apertum.qsystem.server.model.results.QResult;
import ru.apertum.qsystem.server.model.results.QResultList;

/**
 *
 * @author Evgeniy Egorov
 */
public class Form {

    /**
     * Это нужно чтоб делать include во view и потом связывать @Wire("#incClientDashboard #incChangePriorityDialog #changePriorityDlg")
     *
     * @param view
     */
    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
    }

    //*****************************************************
    //**** Логин
    //*****************************************************
    /**
     * Залогиневшейся юзер
     */
    private User user = new User();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Command
    @NotifyChange(value = {"btnsDisabled", "login", "user", "postponList"})
    public void login() {
        System.out.println("---------->>>>>>>> " + user.getName() + "  " + user.getPassword());
        if (login) {
            login = false;
            user.setName("");
            user.setPassword("");
            setKeyRegim(KEYS_OFF);
        } else {
            login = true;

            // TODO for testing
            // need disabled
            user.getPlan().forEach((QPlanService p) -> {
                final CmdParams params = new CmdParams();
                params.serviceId = p.getService().getId();
                Executer.getInstance().getTasks().get(Uses.TASK_STAND_IN).process(params, "", new byte[4]);
            });
            setKeyRegim(KEYS_MAY_INVITE);
        }
        user.getUser().getPlanServiceList().getServices().stream().forEach((ser) -> {
            System.out.println("*** plan " + ser);
        });
    }

    public LinkedList<QUser> getUsersForLogin() {
        return QUserList.getInstance().getItems();
    }

    private boolean login = false;

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    //*********************************************************************************************************
    //**** Кнопки и их события
    //*********************************************************************************************************
    /**
     * текущее состояние кнопок
     */
    private String keys_current = KEYS_OFF;

    /**
     * Механизм включения/отключения кнопок
     *
     * @param regim
     */
    public void setKeyRegim(String regim) {
        keys_current = regim;
        btnsDisabled[0] = !(isLogin() && '1' == regim.charAt(0));
        btnsDisabled[1] = !(isLogin() && '1' == regim.charAt(1));
        btnsDisabled[2] = !(isLogin() && '1' == regim.charAt(2));
        btnsDisabled[3] = !(isLogin() && '1' == regim.charAt(3));
        btnsDisabled[4] = false;//!(isLogin() && '1' == regim.charAt(4));
        btnsDisabled[5] = !(isLogin() && '1' == regim.charAt(5));

    }

    private boolean[] btnsDisabled = new boolean[]{true, true, true, true, false, true};

    public boolean[] getBtnsDisabled() {
        return btnsDisabled;
    }

    public void setBtnsDisabled(boolean[] btnsDisabled) {
        this.btnsDisabled = btnsDisabled;
    }
    private QCustomer customer = null;

    public QCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(QCustomer customer) {
        this.customer = customer;
    }

    @Command
    @NotifyChange(value = {"btnsDisabled", "customer"})
    public void invite() {
        final CmdParams params = new CmdParams();
        params.userId = user.getUser().getId();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + user.getUser().getId());
        final RpcInviteCustomer result = (RpcInviteCustomer) Executer.getInstance().getTasks().get(Uses.TASK_INVITE_NEXT_CUSTOMER).process(params, "", new byte[4]);
        if (result.getResult() != null) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + result.getId());
            customer = result.getResult();
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + result.getResult().getFullNumber());

            if (customer != null && customer.getPostponPeriod() > 0) {
                Messagebox.show("Посетитель был отложен на"
                        + " " + customer.getPostponPeriod() + " "
                        + "минут. И вызван со статусом "
                        + " \"" + customer.getPostponedStatus() + "\".", "Вызов отложенного", Messagebox.OK, Messagebox.INFORMATION);
            }

            setKeyRegim(KEYS_INVITED);
        } else {
            Messagebox.show("Посетителей в очереди нет.", "Вызов следующего", Messagebox.OK, Messagebox.INFORMATION);
        }
        service_list.setModel(service_list.getModel());
    }

    @Command
    @NotifyChange(value = {"btnsDisabled", "customer"})
    public void kill() {
        final CmdParams params = new CmdParams();
        params.userId = user.getUser().getId();
        Executer.getInstance().getTasks().get(Uses.TASK_KILL_NEXT_CUSTOMER).process(params, "", new byte[4]);
        customer = new QCustomer();
        setKeyRegim(KEYS_MAY_INVITE);
        service_list.setModel(service_list.getModel());
    }

    @Command
    @NotifyChange(value = {"btnsDisabled"})
    public void begin() {
        final CmdParams params = new CmdParams();
        params.userId = user.getUser().getId();
        Executer.getInstance().getTasks().get(Uses.TASK_START_CUSTOMER).process(params, "", new byte[4]);

        setKeyRegim(KEYS_STARTED);
        service_list.setModel(service_list.getModel());
    }

    @Command
    @NotifyChange(value = {"btnsDisabled", "customer"})
    public void postpone() {
        postponeCustomerDialog.setVisible(true);
        postponeCustomerDialog.doModal();

        setKeyRegim(KEYS_MAY_INVITE);
        service_list.setModel(service_list.getModel());
    }

    @Wire("#incClientDashboard #service_list")
    private Listbox service_list;

    @Command
    @NotifyChange(value = {"btnsDisabled", "customer", "service_list"})
    public void redirect() {
        redirectCustomerDialog.setVisible(true);
        redirectCustomerDialog.doModal();

        setKeyRegim(KEYS_MAY_INVITE);
        service_list.setModel(service_list.getModel());
    }

    @Command
    @NotifyChange(value = {"btnsDisabled", "customer"})
    public void finish() {
        final CmdParams params = new CmdParams();
        params.userId = user.getUser().getId();
        params.resultId = -1L;
        params.textData = "";
        Executer.getInstance().getTasks().get(Uses.TASK_FINISH_CUSTOMER).process(params, "", new byte[4]);
        customer = null;

        setKeyRegim(KEYS_MAY_INVITE);
        service_list.setModel(service_list.getModel());
    }

    //********************************************************************************************************************************************
    //**  Change priority
    //********************************************************************************************************************************************
    @Wire("#incClientDashboard #incChangePriorityDialog #changePriorityDlg")
    Window changeServicePriorityDialog;
    private QPlanService pickedService;

    public QPlanService getPickedService() {
        return pickedService;
    }

    public void setPickedService(QPlanService pickedService) {
        this.pickedService = pickedService;
    }

    @Command
    //public void clickList(@BindingParam("st") String st) {
    public void clickListServices() {
        if (pickedService != null) {
            if (!pickedService.getFlexible_coef()) {
                Messagebox.show("Изменение приортета этой услуги запрещено.", "Изменение приоритета.", Messagebox.OK, Messagebox.INFORMATION);
                return;
            }
            changeServicePriorityDialog.setVisible(true);
            changeServicePriorityDialog.doModal();
        }
    }

    public LinkedList<String> prior_St = new LinkedList(Uses.get_COEFF_WORD().values());

    public LinkedList<String> getPrior_St() {
        return prior_St;
    }

    public void setPrior_St(LinkedList<String> prior_St) {
        this.prior_St = prior_St;
    }

    @Command
    @NotifyChange(value = {"user"})
    public void closeChangePriorityDialog() {
        changeServicePriorityDialog.setVisible(false);
    }

    //********************************************************************************************************************************************
    //** Отложенные Отложить посетителя
    //********************************************************************************************************************************************
    @Wire("#incClientDashboard #incPostponeCustomerDialog #postponeDialog")
    Window postponeCustomerDialog;

    @Command
    public void closePostponeCustomerDialog() {
        postponeCustomerDialog.setVisible(false);
    }

    @Command
    @NotifyChange(value = {"postponList", "customer", "user"})
    public void OKPostponeCustomerDialog() {
        final CmdParams params = new CmdParams();
        params.userId = user.getUser().getId();
        params.textData = ((Combobox) postponeCustomerDialog.getFellow("resultBox")).getSelectedItem().getLabel();
        params.postponedPeriod = ((Combobox) postponeCustomerDialog.getFellow("timeBox")).getSelectedIndex() * 5;
        Executer.getInstance().getTasks().get(Uses.TASK_CUSTOMER_TO_POSTPON).process(params, "", new byte[4]);
        customer = null;

        postponeCustomerDialog.setVisible(false);
    }

    private final LinkedList<QCustomer> postponList = QPostponedList.getInstance().getPostponedCustomers();

    public LinkedList<QCustomer> getPostponList() {
        return QPostponedList.getInstance().getPostponedCustomers();
    }

    private final LinkedList<QResult> resultList = QResultList.getInstance().getItems();

    public LinkedList<QResult> getResultList() {
        return QResultList.getInstance().getItems();
    }

    private QCustomer pickedPostponed;

    public QCustomer getPickedPostponed() {
        return pickedPostponed;
    }

    public void setPickedPostponed(QCustomer pickedPostponed) {
        this.pickedPostponed = pickedPostponed;
    }

    // *** Диалоги изменения состояния и вызова лтложенных
    @Wire("#incClientDashboard #incChangePostponedStatusDialog #changePostponedStatusDialog")
    Window changePostponedStatusDialog;

    @Command
    public void clickListPostponedChangeStatus() {
        ((Combobox) changePostponedStatusDialog.getFellow("pndResultBox")).setText(pickedPostponed.getPostponedStatus());
        changePostponedStatusDialog.setVisible(true);
        changePostponedStatusDialog.doModal();
    }

    @Command
    @NotifyChange(value = {"postponList"})
    public void closeChangePostponedStatusDialog() {
        final CmdParams params = new CmdParams();
        // кому
        params.customerId = pickedPostponed.getId();
        // на что
        params.textData = ((Combobox) changePostponedStatusDialog.getFellow("pndResultBox")).getText();
        Executer.getInstance().getTasks().get(Uses.TASK_POSTPON_CHANGE_STATUS).process(params, "", new byte[4]);

        changePostponedStatusDialog.setVisible(false);
    }

    @Command
    public void clickListPostponedInvite() {
        Messagebox.show("Хотите вызвать отложенного посетителя?", "Вызов посетителя", new Messagebox.Button[]{
            Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, new EventListener<Messagebox.ClickEvent>() {

            @Override
            public void onEvent(Messagebox.ClickEvent t) throws Exception {
                if (t.getButton() != null && t.getButton().compareTo(Messagebox.Button.YES) == 0) {
                    final CmdParams params = new CmdParams();
                    // @param userId id юзера который вызывает
                    // @param id это ID кастомера которого вызываем из пула отложенных, оно есть т.к. с качстомером давно работаем
                    params.customerId = pickedPostponed.getId();
                    params.userId = user.getUser().getId();
                    Executer.getInstance().getTasks().get(Uses.TASK_INVITE_POSTPONED).process(params, "", new byte[4]);
                    customer = user.getUser().getCustomer();

                    setKeyRegim(KEYS_INVITED);
                    BindUtils.postNotifyChange(null, null, Form.this, "postponList");
                    BindUtils.postNotifyChange(null, null, Form.this, "customer");
                    BindUtils.postNotifyChange(null, null, Form.this, "btnsDisabled");

                }
            }
        });
    }
    //********************************************************************************************************************************************
    //** Перенаправление
    //********************************************************************************************************************************************
    private TreeServices treeServs = TreeServices.getInstance();

    public TreeServices getTreeServs() {
        return treeServs;
    }

    public void setTreeServs(TreeServices treeServs) {
        this.treeServs = treeServs;
    }

    @Wire("#incClientDashboard #incRedirectCustomerDialog #redirectDialog")
    Window redirectCustomerDialog;

    @Command
    @NotifyChange(value = {"postponList", "customer", "user"})
    public void closeRedirectDialog() {

        if (pickedRedirectServ != null) {
            if (!pickedRedirectServ.isLeaf()) {
                Messagebox.show("Выбранная услуга является группой, в группу не возможно перенаправить.", "Выбор услуги", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }

            final CmdParams params = new CmdParams();

            params.userId = user.getUser().getId();
            params.serviceId = pickedRedirectServ.getId();
            params.requestBack = ((Checkbox) redirectCustomerDialog.getFellow("cb_redirect")).isChecked();
            params.resultId = -1l;
            params.textData = ((Textbox) redirectCustomerDialog.getFellow("tb_redirect")).getText();
            Executer.getInstance().getTasks().get(Uses.TASK_REDIRECT_CUSTOMER).process(params, "", new byte[4]);
            customer = null;

            redirectCustomerDialog.setVisible(false);

        }
    }
    QService pickedRedirectServ;

    public QService getPickedRedirectServ() {
        return pickedRedirectServ;
    }

    public void setPickedRedirectServ(QService pickedRedirectServ) {
        this.pickedRedirectServ = pickedRedirectServ;
    }

}
