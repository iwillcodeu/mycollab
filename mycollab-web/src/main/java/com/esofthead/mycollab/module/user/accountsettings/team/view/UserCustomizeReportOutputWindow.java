/**
 * This file is part of mycollab-web.
 *
 * mycollab-web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-web.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.user.accountsettings.team.view;

import com.esofthead.mycollab.common.TableViewField;
import com.esofthead.mycollab.core.db.query.VariableInjector;
import com.esofthead.mycollab.module.user.AdminTypeConstants;
import com.esofthead.mycollab.module.user.accountsettings.localization.UserI18nEnum;
import com.esofthead.mycollab.module.user.accountsettings.view.UserTableFieldDef;
import com.esofthead.mycollab.module.user.domain.SimpleUser;
import com.esofthead.mycollab.module.user.domain.criteria.UserSearchCriteria;
import com.esofthead.mycollab.module.user.service.UserService;
import com.esofthead.mycollab.reporting.CustomizeReportOutputWindow;
import com.esofthead.mycollab.spring.AppContextUtil;
import com.esofthead.mycollab.vaadin.AppContext;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author MyCollab Ltd
 * @since 5.3.4
 */
public class UserCustomizeReportOutputWindow extends CustomizeReportOutputWindow<UserSearchCriteria, SimpleUser> {
    public UserCustomizeReportOutputWindow(VariableInjector<UserSearchCriteria> variableInjector) {
        super(AdminTypeConstants.USER, AppContext.getMessage(UserI18nEnum.LIST), SimpleUser.class,
                AppContextUtil.getSpringBean(UserService.class), variableInjector);
    }

    @Override
    protected Collection<TableViewField> getDefaultColumns() {
        return Arrays.asList(UserTableFieldDef.displayName(), UserTableFieldDef.roleName(),
                UserTableFieldDef.email(), UserTableFieldDef.birthday(),
                UserTableFieldDef.officePhone(), UserTableFieldDef.homePhone(), UserTableFieldDef.company());
    }

    @Override
    protected Collection<TableViewField> getAvailableColumns() {
        return Arrays.asList(UserTableFieldDef.displayName(), UserTableFieldDef.roleName(),
                UserTableFieldDef.email(), UserTableFieldDef.birthday(),
                UserTableFieldDef.officePhone(), UserTableFieldDef.homePhone(), UserTableFieldDef.company());
    }

    @Override
    protected Object[] buildSampleData() {
        return new Object[]{"John Adams", "Administrator", "john.adam@mycollab.com", AppContext.formatDate(new
                LocalDate(1979, 3, 13).toDate()), "11111111", "11111111", "MyCollab"};
    }
}
