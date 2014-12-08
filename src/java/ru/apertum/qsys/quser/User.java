package ru.apertum.qsys.quser;

import java.util.LinkedList;
import java.util.List;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.server.model.QPlanService;
import ru.apertum.qsystem.server.model.QServiceTree;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;

public class User {

    private String name = "";
    private String password = "";
    private QUser user;
    private List<QPlanService> plan = new LinkedList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        for (QUser us : QUserList.getInstance().getItems()) {
            if (us.getName().equalsIgnoreCase(name)) {
                user = us;
                plan = user.getPlanServiceList().getServices();
                return;
            }
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public QUser getUser() {
        return user;
    }

    public void setUser(QUser user) {
        this.user = user;
    }

    public List<QPlanService> getPlan() {
        return plan;
    }

    public void setPlan(List<QPlanService> plan) {
        this.plan = plan;
    }

    public int getLineSize(long serviceId) {
        return QServiceTree.getInstance().getById(serviceId).getCountCustomers();
    }

    public String getPriority(long serviceId) {
        for (QPlanService qPlanService : plan) {
            if (qPlanService.getService().getId().equals(serviceId)) {
                return Uses.get_COEFF_WORD().get(qPlanService.getCoefficient());
            }
        }
        return "--";
    }

    @Override
    public String toString() {
        return name;
    }

}
