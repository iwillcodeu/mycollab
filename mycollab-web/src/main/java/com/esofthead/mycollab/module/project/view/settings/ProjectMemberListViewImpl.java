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
package com.esofthead.mycollab.module.project.view.settings;

import com.esofthead.mycollab.common.GenericLinkUtils;
import com.esofthead.mycollab.common.i18n.GenericI18Enum;
import com.esofthead.mycollab.core.arguments.BasicSearchRequest;
import com.esofthead.mycollab.core.arguments.SearchCriteria;
import com.esofthead.mycollab.core.arguments.StringSearchField;
import com.esofthead.mycollab.core.db.query.LazyValueInjector;
import com.esofthead.mycollab.core.utils.NumberUtils;
import com.esofthead.mycollab.eventmanager.EventBusFactory;
import com.esofthead.mycollab.module.project.*;
import com.esofthead.mycollab.module.project.domain.SimpleProjectMember;
import com.esofthead.mycollab.module.project.domain.criteria.ProjectMemberSearchCriteria;
import com.esofthead.mycollab.module.project.events.ProjectMemberEvent;
import com.esofthead.mycollab.module.project.i18n.ProjectCommonI18nEnum;
import com.esofthead.mycollab.module.project.i18n.ProjectMemberI18nEnum;
import com.esofthead.mycollab.module.project.i18n.TimeTrackingI18nEnum;
import com.esofthead.mycollab.module.project.service.ProjectMemberService;
import com.esofthead.mycollab.module.project.ui.ProjectAssetsManager;
import com.esofthead.mycollab.module.project.ui.components.ComponentUtils;
import com.esofthead.mycollab.module.user.accountsettings.localization.UserI18nEnum;
import com.esofthead.mycollab.spring.AppContextUtil;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.mvp.AbstractPageView;
import com.esofthead.mycollab.vaadin.mvp.ViewComponent;
import com.esofthead.mycollab.vaadin.ui.ELabel;
import com.esofthead.mycollab.vaadin.ui.HeaderWithFontAwesome;
import com.esofthead.mycollab.vaadin.ui.UserAvatarControlFactory;
import com.esofthead.mycollab.vaadin.web.ui.ConfirmDialogExt;
import com.esofthead.mycollab.vaadin.web.ui.SearchTextField;
import com.esofthead.mycollab.vaadin.web.ui.UIConstants;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Span;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.Collections;
import java.util.List;

/**
 * @author MyCollab Ltd.
 * @since 1.0
 */
@ViewComponent
public class ProjectMemberListViewImpl extends AbstractPageView implements ProjectMemberListView {
    private static final long serialVersionUID = 1L;
    private CssLayout contentLayout;
    private HeaderWithFontAwesome headerText;
    private boolean sortAsc = true;
    private ProjectMemberSearchCriteria searchCriteria;

    public ProjectMemberListViewImpl() {
        super();
        this.setMargin(new MarginInfo(false, true, true, true));
        MHorizontalLayout viewHeader = new MHorizontalLayout().withMargin(new MarginInfo(true, false, true, false)).withFullWidth();
        viewHeader.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        headerText = ComponentUtils.headerH2(ProjectTypeConstants.MEMBER, AppContext.getMessage(ProjectMemberI18nEnum.LIST));
        viewHeader.with(headerText).expand(headerText);

        final Button sortBtn = new Button();
        sortBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                sortAsc = !sortAsc;
                if (sortAsc) {
                    sortBtn.setIcon(FontAwesome.SORT_ALPHA_ASC);
                    displayMembers();
                } else {
                    sortBtn.setIcon(FontAwesome.SORT_ALPHA_DESC);
                    displayMembers();
                }
            }
        });
        sortBtn.setIcon(FontAwesome.SORT_ALPHA_ASC);
        sortBtn.addStyleName(UIConstants.BUTTON_ICON_ONLY);
        viewHeader.addComponent(sortBtn);

        final SearchTextField searchTextField = new SearchTextField() {
            @Override
            public void doSearch(String value) {
                searchCriteria.setMemberFullName(StringSearchField.and(value));
                displayMembers();
            }

            @Override
            public void emptySearch() {
                searchCriteria.setMemberFullName(null);
                displayMembers();
            }
        };
        searchTextField.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        viewHeader.addComponent(searchTextField);

        Button printBtn = new Button("", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                UI.getCurrent().addWindow(new ProjectMemberCustomizeReportOutputWindow(new LazyValueInjector() {
                    @Override
                    protected Object doEval() {
                        return searchCriteria;
                    }
                }));
            }
        });
        printBtn.setIcon(FontAwesome.PRINT);
        printBtn.addStyleName(UIConstants.BUTTON_OPTION);
        printBtn.setDescription(AppContext.getMessage(GenericI18Enum.ACTION_EXPORT));
        viewHeader.addComponent(printBtn);

        Button createBtn = new Button(AppContext.getMessage(ProjectMemberI18nEnum.BUTTON_NEW_INVITEES), new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                EventBusFactory.getInstance().post(new ProjectMemberEvent.GotoInviteMembers(this, null));
            }
        });
        createBtn.setEnabled(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.USERS));
        createBtn.setStyleName(UIConstants.BUTTON_ACTION);
        createBtn.setIcon(FontAwesome.SEND);
        viewHeader.addComponent(createBtn);

        addComponent(viewHeader);

        contentLayout = new CssLayout();
        contentLayout.setWidth("100%");
        addComponent(contentLayout);
    }

    @Override
    public void setSearchCriteria(ProjectMemberSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
        displayMembers();
    }

    private void displayMembers() {
        contentLayout.removeAllComponents();
        if (sortAsc) {
            searchCriteria.setOrderFields(Collections.singletonList(new SearchCriteria.OrderField("memberFullName", SearchCriteria.ASC)));
        } else {
            searchCriteria.setOrderFields(Collections.singletonList(new SearchCriteria.OrderField("memberFullName",
                    SearchCriteria.DESC)));
        }
        ProjectMemberService prjMemberService = AppContextUtil.getSpringBean(ProjectMemberService.class);
        List<SimpleProjectMember> memberLists = prjMemberService.findPagableListByCriteria(new BasicSearchRequest<>(searchCriteria, 0,
                Integer.MAX_VALUE));

        headerText.updateTitle(String.format("%s (%d)", AppContext.getMessage(ProjectMemberI18nEnum.LIST), memberLists.size()));
        for (SimpleProjectMember member : memberLists) {
            contentLayout.addComponent(generateMemberBlock(member));
        }
    }

    private Component generateMemberBlock(final SimpleProjectMember member) {
        HorizontalLayout blockContent = new HorizontalLayout();
        blockContent.setStyleName("member-block");
        if (ProjectMemberStatusConstants.NOT_ACCESS_YET.equals(member.getStatus())) {
            blockContent.addStyleName("inactive");
        }
        blockContent.setWidth("350px");

        Image memberAvatar = UserAvatarControlFactory.createUserAvatarEmbeddedComponent(member.getMemberAvatarId(), 100);
        memberAvatar.addStyleName(UIConstants.CIRCLE_BOX);
        memberAvatar.setWidthUndefined();
        blockContent.addComponent(memberAvatar);

        MVerticalLayout blockTop = new MVerticalLayout().withMargin(new MarginInfo(false, false, false, true)).withFullWidth();

        MHorizontalLayout buttonControls = new MHorizontalLayout();

        Button editBtn = new Button("", FontAwesome.EDIT);
        editBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                EventBusFactory.getInstance().post(new ProjectMemberEvent.GotoEdit(ProjectMemberListViewImpl.this, member));
            }
        });
        editBtn.setVisible(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.USERS));
        editBtn.setDescription("Edit user '" + member.getDisplayName() + "' information");
        editBtn.addStyleName(UIConstants.BUTTON_LINK);

        Button deleteBtn = new Button("", FontAwesome.TRASH_O);
        deleteBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent clickEvent) {
                ConfirmDialogExt.show(UI.getCurrent(),
                        AppContext.getMessage(GenericI18Enum.DIALOG_DELETE_TITLE, AppContext.getSiteName()),
                        AppContext.getMessage(GenericI18Enum.DIALOG_DELETE_SINGLE_ITEM_MESSAGE),
                        AppContext.getMessage(GenericI18Enum.BUTTON_YES),
                        AppContext.getMessage(GenericI18Enum.BUTTON_NO),
                        new ConfirmDialog.Listener() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    ProjectMemberService prjMemberService = AppContextUtil.getSpringBean(ProjectMemberService.class);
                                    prjMemberService.removeWithSession(member, AppContext.getUsername(), AppContext.getAccountId());

                                    EventBusFactory.getInstance().post(new ProjectMemberEvent.GotoList(ProjectMemberListViewImpl.this, null));
                                }
                            }
                        });
            }
        });
        deleteBtn.setDescription("Remove user '" + member.getDisplayName() + "' out of this project");
        deleteBtn.addStyleName(UIConstants.BUTTON_LINK);
        deleteBtn.setVisible(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.USERS));

        buttonControls.with(editBtn, deleteBtn);
        blockTop.addComponent(buttonControls);
        blockTop.setComponentAlignment(buttonControls, Alignment.TOP_RIGHT);

        A memberLink = new A(ProjectLinkBuilder.generateProjectMemberFullLink(member.getProjectid(), member
                .getUsername())).appendText(member.getMemberFullName()).setTitle(member.getMemberFullName());
        ELabel memberNameLbl = ELabel.h3(memberLink.write()).withStyleName(UIConstants.TEXT_ELLIPSIS).withFullWidth();

        blockTop.with(memberNameLbl, ELabel.hr());

        String roleLink = String.format("<a href=\"%s%s%s\"", AppContext.getSiteUrl(), GenericLinkUtils.URL_PREFIX_PARAM,
                ProjectLinkGenerator.generateRolePreviewLink(member.getProjectid(), member.getProjectroleid()));
        ELabel memberRole = new ELabel("", ContentMode.HTML).withFullWidth().withStyleName(UIConstants.TEXT_ELLIPSIS);
        if (member.isProjectOwner()) {
            memberRole.setValue(roleLink + "style=\"color: #B00000;\">" + "Project Owner" + "</a>");
        } else {
            memberRole.setValue(roleLink + "style=\"color:gray;font-size:12px;\">" + member.getRoleName() + "</a>");
        }
        blockTop.addComponent(memberRole);

        if (Boolean.TRUE.equals(AppContext.showEmailPublicly())) {
            Label memberEmailLabel = new Label(String.format("<a href='mailto:%s'>%s</a>", member.getUsername(),
                    member.getUsername()), ContentMode.HTML);
            memberEmailLabel.addStyleName(UIConstants.META_INFO);
            memberEmailLabel.setWidth("100%");
            blockTop.addComponent(memberEmailLabel);
        }

        ELabel memberSinceLabel = new ELabel(AppContext.getMessage(UserI18nEnum.OPT_MEMBER_SINCE, AppContext.formatPrettyTime(member.getJoindate())))
                .withDescription(AppContext.formatDateTime(member.getJoindate()));
        memberSinceLabel.addStyleName(UIConstants.META_INFO);
        memberSinceLabel.setWidth("100%");
        blockTop.addComponent(memberSinceLabel);

        if (ProjectMemberStatusConstants.ACTIVE.equals(member.getStatus())) {
            ELabel lastAccessTimeLbl = new ELabel(AppContext.getMessage(UserI18nEnum.OPT_MEMBER_LOGGED_IN, AppContext.formatPrettyTime(member.getLastAccessTime())))
                    .withDescription(AppContext.formatDateTime(member.getLastAccessTime()));
            lastAccessTimeLbl.addStyleName(UIConstants.META_INFO);
            blockTop.addComponent(lastAccessTimeLbl);
        }

        String memberWorksInfo = ProjectAssetsManager.getAsset(ProjectTypeConstants.TASK).getHtml() + " " + new Span()
                .appendText("" + member.getNumOpenTasks()).setTitle(AppContext.getMessage(ProjectCommonI18nEnum.OPT_OPEN_TASKS)) +
                "  " + ProjectAssetsManager.getAsset(ProjectTypeConstants.BUG).getHtml() + " " + new Span()
                .appendText("" + member.getNumOpenBugs()).setTitle(AppContext.getMessage(ProjectCommonI18nEnum.OPT_OPEN_BUGS)) +
                " " + FontAwesome.MONEY.getHtml() + " " + new Span().appendText("" + NumberUtils.roundDouble(2,
                member.getTotalBillableLogTime())).setTitle(AppContext.getMessage(TimeTrackingI18nEnum.OPT_BILLABLE_HOURS)) +
                "  " + FontAwesome.GIFT.getHtml() +
                " " + new Span().appendText("" + NumberUtils.roundDouble(2, member.getTotalNonBillableLogTime()))
                .setTitle(AppContext.getMessage(TimeTrackingI18nEnum.OPT_NON_BILLABLE_HOURS));

        Label memberWorkStatus = new Label(memberWorksInfo, ContentMode.HTML);
        memberWorkStatus.addStyleName(UIConstants.META_INFO);
        blockTop.addComponent(memberWorkStatus);

        blockContent.addComponent(blockTop);
        blockContent.setExpandRatio(blockTop, 1.0f);
        return blockContent;
    }
}