/**
 * This file is part of mycollab-localization.
 *
 * mycollab-localization is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-localization is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-localization.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.project.i18n;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("project")
@LocaleData(value = {@Locale("en-US")}, defaultCharset = "UTF-8")
public enum ProjectI18nEnum {
    NEW,
    EDIT,
    LIST,
    SINGLE,

    FORM_HOME_PAGE,
    FORM_ACCOUNT_NAME,
    FORM_ACCOUNT_NAME_HELP,
    FORM_SHORT_NAME,
    FORM_SHORT_NAME_HELP,
    FORM_BILLING_RATE,
    FORM_BILLING_RATE_HELP,
    FORM_OVERTIME_BILLING_RATE,
    FORM_OVERTIME_BILLING_RATE_HELP,
    FORM_CURRENCY_HELP,
    FORM_TARGET_BUDGET,
    FORM_TARGET_BUDGET_HELP,
    FORM_ACTUAL_BUDGET,
    FORM_ACTUAL_BUDGET_HELP,
    FORM_LEADER,
    FORM_TEMPLATE,

    ACTION_ADD_MEMBERS,
    ACTION_BROWSE,
    ACTION_COLLAPSE_MENU,
    ACTION_EXPAND_MENU,
    ACTION_MARK_TEMPLATE,
    ACTION_UNMARK_TEMPLATE,
    ACTION_CHANGE_LOGO,
    ACTION_QUERY_BY_PROJECT_NAME,
    ACTION_VIEW_ASSIGNMENTS,
    ACTION_HIDE_ASSIGNMENTS,

    SECTION_PROJECT_INFO,
    SECTION_FINANCE_SCHEDULE,
    SECTION_DESCRIPTION,

    OPT_CREATE_PROJECT_FROM_TEMPLATE,
    OPT_MARK_TEMPLATE_HELP,
    OPT_TO_ADD_PROJECT,
    OPT_ASK_TO_ADD_MEMBERS,
    OPT_PROJECT_ASSIGNMENT,
    OPT_NO_ASSIGNMENT,
    OPT_NO_OVERDUE_ASSIGNMENT,
    OPT_UNRESOLVED_ASSIGNMENT_THIS_WEEK,
    OPT_UNRESOLVED_ASSIGNMENT_NEXT_WEEK,
    OPT_SEARCH_TERM,

    ERROR_MUST_CHOOSE_TEMPLATE_PROJECT
}
