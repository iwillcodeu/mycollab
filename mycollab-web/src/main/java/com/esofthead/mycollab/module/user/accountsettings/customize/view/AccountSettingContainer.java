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
package com.esofthead.mycollab.module.user.accountsettings.customize.view;

import com.esofthead.mycollab.configuration.SiteConfiguration;
import com.esofthead.mycollab.module.user.accountsettings.localization.AdminI18nEnum;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.mvp.AbstractPageView;
import com.esofthead.mycollab.vaadin.mvp.PresenterResolver;
import com.esofthead.mycollab.vaadin.mvp.ViewComponent;
import com.esofthead.mycollab.vaadin.web.ui.TabSheetDecorator;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;

/**
 * @author MyCollab Ltd.
 * @since 4.1
 */
@ViewComponent
public class AccountSettingContainer extends AbstractPageView {
    private static final long serialVersionUID = -1923841035522809056L;

    private GeneralSettingPresenter generalSettingPresenter;
    private IThemeCustomizePresenter themeCustomizePresenter;

    private final TabSheetDecorator settingTab;

    private String selectedTabId = "";

    public AccountSettingContainer() {
        settingTab = new TabSheetDecorator();
        this.addComponent(settingTab);
        this.withFullWidth();
        this.buildComponents();
    }

    private void buildComponents() {
        generalSettingPresenter = PresenterResolver.getPresenter(GeneralSettingPresenter.class);
        settingTab.addTab(generalSettingPresenter.getView(), AppContext.getMessage(AdminI18nEnum.OPT_GENERAL_SETTINGS));

        if (!SiteConfiguration.isCommunityEdition()) {
            themeCustomizePresenter = PresenterResolver.getPresenter(IThemeCustomizePresenter.class);
            settingTab.addTab(this.themeCustomizePresenter.getView(), AppContext.getMessage(AdminI18nEnum.OPT_THEME));
        }

        settingTab.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                TabSheet.Tab tab = ((TabSheetDecorator) event.getTabSheet()).getSelectedTabInfo();
                String caption = tab.getCaption();
                if (AppContext.getMessage(AdminI18nEnum.OPT_GENERAL_SETTINGS).equals(caption)
                        && !AppContext.getMessage(AdminI18nEnum.OPT_GENERAL_SETTINGS).equals(selectedTabId)) {
                    generalSettingPresenter.go(AccountSettingContainer.this, null);
                } else if (AppContext.getMessage(AdminI18nEnum.OPT_THEME).equals(caption)
                        && !AppContext.getMessage(AdminI18nEnum.OPT_THEME).equals(selectedTabId)) {
                    themeCustomizePresenter.go(AccountSettingContainer.this, null);
                }
                selectedTabId = "";
            }
        });
    }

    public Component gotoSubView(String name) {
        selectedTabId = name;
        return settingTab.selectTab(name).getComponent();
    }
}
