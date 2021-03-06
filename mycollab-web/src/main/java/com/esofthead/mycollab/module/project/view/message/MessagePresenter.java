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

package com.esofthead.mycollab.module.project.view.message;

import com.esofthead.mycollab.core.arguments.SetSearchField;
import com.esofthead.mycollab.module.project.CurrentProjectVariables;
import com.esofthead.mycollab.module.project.ProjectRolePermissionCollections;
import com.esofthead.mycollab.module.project.ProjectTypeConstants;
import com.esofthead.mycollab.module.project.domain.criteria.MessageSearchCriteria;
import com.esofthead.mycollab.module.project.view.ProjectView;
import com.esofthead.mycollab.module.project.view.parameters.MessageScreenData;
import com.esofthead.mycollab.vaadin.mvp.PresenterResolver;
import com.esofthead.mycollab.vaadin.mvp.ScreenData;
import com.esofthead.mycollab.vaadin.ui.NotificationUtil;
import com.esofthead.mycollab.vaadin.web.ui.AbstractPresenter;
import com.vaadin.ui.ComponentContainer;

/**
 * @author MyCollab Ltd.
 * @since 1.0
 */
public class MessagePresenter extends AbstractPresenter<MessageContainer> {
    private static final long serialVersionUID = 1L;

    public MessagePresenter() {
        super(MessageContainer.class);
    }

    @Override
    protected void onGo(ComponentContainer container, ScreenData<?> data) {
        if (CurrentProjectVariables.canRead(ProjectRolePermissionCollections.MESSAGES)) {
            ProjectView projectViewContainer = (ProjectView) container;
            projectViewContainer.gotoSubView(ProjectTypeConstants.MESSAGE);

            view.removeAllComponents();

            if (data instanceof MessageScreenData.Read) {
                MessageReadPresenter presenter = PresenterResolver.getPresenter(MessageReadPresenter.class);
                presenter.go(view, data);
            } else if (data instanceof MessageScreenData.Search) {
                MessageListPresenter presenter = PresenterResolver.getPresenter(MessageListPresenter.class);
                presenter.go(view, data);
            } else if (data == null) {
                MessageSearchCriteria searchCriteria = new MessageSearchCriteria();
                searchCriteria.setProjectids(new SetSearchField<>(CurrentProjectVariables.getProjectId()));
                MessageListPresenter presenter = PresenterResolver.getPresenter(MessageListPresenter.class);
                presenter.go(view, new ScreenData.Preview<>(searchCriteria));
            }
        } else {
            NotificationUtil.showMessagePermissionAlert();
        }
    }

}
