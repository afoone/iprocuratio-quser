package ru.apertum.qsys.quser;

import java.util.Map;
import org.zkoss.bind.Property;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;

public class UserLoginValidator extends AbstractValidator {

    @Override
    public void validate(ValidationContext ctx) {
        //all the bean properties
        Map<String, Property> beanProps = ctx.getProperties(ctx.getProperty().getBase());
        validateName(ctx, (String) beanProps.get("name").getValue());
        validatePassword(ctx, (String) beanProps.get("name").getValue(), (String) beanProps.get("password").getValue());
    }

    private void validateName(ValidationContext ctx, String name) {
        if (name == null || name.isEmpty()) {
            this.addInvalidMessage(ctx, "name", "Не найден пользователь вообще!");
        }
        if (name != null && !name.isEmpty() && !QUserList.getInstance().hasByName(name)) {
            this.addInvalidMessage(ctx, "name", "Не найден пользователь " + name + "!");
        }
    }

    private void validatePassword(ValidationContext ctx, String name, String pass) {
        for (QUser user : QUserList.getInstance().getItems()) {
            if (user.getName().equalsIgnoreCase(name) && user.isCorrectPassword(pass)) {
                return;
            }
        }
        this.addInvalidMessage(ctx, "name", "Нет доступа!");
    }

}
