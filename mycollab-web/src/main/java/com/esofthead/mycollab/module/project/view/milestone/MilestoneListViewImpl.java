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
package com.esofthead.mycollab.module.project.view.milestone;

import com.esofthead.mycollab.common.i18n.GenericI18Enum;
import com.esofthead.mycollab.configuration.SiteConfiguration;
import com.esofthead.mycollab.core.arguments.SetSearchField;
import com.esofthead.mycollab.core.db.query.LazyValueInjector;
import com.esofthead.mycollab.eventmanager.ApplicationEventListener;
import com.esofthead.mycollab.eventmanager.EventBusFactory;
import com.esofthead.mycollab.module.project.CurrentProjectVariables;
import com.esofthead.mycollab.module.project.ProjectRolePermissionCollections;
import com.esofthead.mycollab.module.project.ProjectTypeConstants;
import com.esofthead.mycollab.module.project.domain.SimpleMilestone;
import com.esofthead.mycollab.module.project.domain.criteria.MilestoneSearchCriteria;
import com.esofthead.mycollab.module.project.events.MilestoneEvent;
import com.esofthead.mycollab.module.project.i18n.MilestoneI18nEnum;
import com.esofthead.mycollab.module.project.i18n.OptionI18nEnum.MilestoneStatus;
import com.esofthead.mycollab.module.project.i18n.ProjectI18nEnum;
import com.esofthead.mycollab.module.project.service.MilestoneService;
import com.esofthead.mycollab.module.project.ui.components.ComponentUtils;
import com.esofthead.mycollab.reporting.ReportExportType;
import com.esofthead.mycollab.reporting.ReportStreamSource;
import com.esofthead.mycollab.reporting.RpFieldsBuilder;
import com.esofthead.mycollab.reporting.SimpleReportTemplateExecutor;
import com.esofthead.mycollab.spring.AppContextUtil;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.mvp.ViewComponent;
import com.esofthead.mycollab.vaadin.mvp.ViewManager;
import com.esofthead.mycollab.vaadin.mvp.view.AbstractLazyPageView;
import com.esofthead.mycollab.vaadin.ui.ELabel;
import com.esofthead.mycollab.vaadin.ui.HeaderWithFontAwesome;
import com.esofthead.mycollab.vaadin.web.ui.ConfirmDialogExt;
import com.esofthead.mycollab.vaadin.web.ui.OptionPopupContent;
import com.esofthead.mycollab.vaadin.web.ui.ToggleButtonGroup;
import com.esofthead.mycollab.vaadin.web.ui.UIConstants;
import com.esofthead.mycollab.web.CustomLayoutExt;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.teemu.VaadinIcons;
import org.vaadin.viritin.layouts.MHorizontalLayout;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author MyCollab Ltd.
 * @since 1.0
 */
@ViewComponent
public class MilestoneListViewImpl extends AbstractLazyPageView implements MilestoneListView {
    private static final long serialVersionUID = 1L;

    private CssLayout inProgressContainer;
    private Label inProgressHeader;

    private CssLayout futureContainer;
    private Label futureHeader;

    private CssLayout closeContainer;
    private Label closedHeader;

    private MilestoneSearchCriteria baseCriteria;

    private ApplicationEventListener<MilestoneEvent.NewMilestoneAdded> newMilestoneHandler = new
            ApplicationEventListener<MilestoneEvent.NewMilestoneAdded>() {
                @Override
                @Subscribe
                public void handle(MilestoneEvent.NewMilestoneAdded event) {
                    displayMilestones();
                }
            };

    @Override
    public void attach() {
        EventBusFactory.getInstance().register(newMilestoneHandler);
        super.attach();
    }

    @Override
    public void detach() {
        EventBusFactory.getInstance().unregister(newMilestoneHandler);
        super.detach();
    }

    @Override
    protected void displayView() {
        initUI();
        constructBody();
        displayMilestones();
    }

    private void displayMilestones() {
        closeContainer.removeAllComponents();
        inProgressContainer.removeAllComponents();
        futureContainer.removeAllComponents();
        baseCriteria = new MilestoneSearchCriteria();
        baseCriteria.setProjectIds(new SetSearchField<>(CurrentProjectVariables.getProjectId()));
        MilestoneService milestoneService = AppContextUtil.getSpringBean(MilestoneService.class);
        List<SimpleMilestone> milestones = milestoneService.findAbsoluteListByCriteria(baseCriteria, 0, Integer.MAX_VALUE);
        int totalClosedMilestones = 0, totalInprogressMilestones = 0, totalFutureMilestones = 0;

        for (SimpleMilestone milestone : milestones) {
            ComponentContainer componentContainer = constructMilestoneBox(milestone);
            if (MilestoneStatus.InProgress.name().equals(milestone.getStatus())) {
                inProgressContainer.addComponent(componentContainer);
                totalInprogressMilestones++;
            } else if (MilestoneStatus.Future.name().equals(milestone.getStatus())) {
                futureContainer.addComponent(componentContainer);
                totalFutureMilestones++;
            } else if (MilestoneStatus.Closed.name().equals(milestone.getStatus())) {
                closeContainer.addComponent(componentContainer);
                totalClosedMilestones++;
            }
        }

        updateClosedMilestoneNumber(totalClosedMilestones);
        updateFutureMilestoneNumber(totalFutureMilestones);
        updateInProgressMilestoneNumber(totalInprogressMilestones);
    }

    private void initUI() {
        HeaderWithFontAwesome headerText = ComponentUtils.headerH2(ProjectTypeConstants.MILESTONE, AppContext.getMessage
                (MilestoneI18nEnum.LIST));

        MHorizontalLayout header = new MHorizontalLayout().withStyleName("hdr-view").withFullWidth().withMargin(true)
                .with(headerText, createHeaderRight()).withAlign(headerText, Alignment.MIDDLE_LEFT).expand(headerText);
        this.addComponent(header);
    }

    private HorizontalLayout createHeaderRight() {
        MHorizontalLayout layout = new MHorizontalLayout();

        Button createBtn = new Button(AppContext.getMessage(MilestoneI18nEnum.NEW), new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                SimpleMilestone milestone = new SimpleMilestone();
                milestone.setSaccountid(AppContext.getAccountId());
                milestone.setProjectid(CurrentProjectVariables.getProjectId());
                UI.getCurrent().addWindow(new MilestoneAddWindow(milestone));
            }
        });
        createBtn.setIcon(FontAwesome.PLUS);
        createBtn.setStyleName(UIConstants.BUTTON_ACTION);
        createBtn.setEnabled(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.MILESTONES));
        layout.with(createBtn);

        Button printBtn = new Button("", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                UI.getCurrent().addWindow(new MilestoneCustomizeReportOutputWindow(new LazyValueInjector() {
                    @Override
                    protected Object doEval() {
                        return baseCriteria;
                    }
                }));
            }
        });
        printBtn.setIcon(FontAwesome.PRINT);
        printBtn.addStyleName(UIConstants.BUTTON_OPTION);
        printBtn.setDescription(AppContext.getMessage(GenericI18Enum.ACTION_EXPORT));
        layout.addComponent(printBtn);

        Button kanbanBtn = new Button("Board");
        kanbanBtn.setDescription("Board View");
        kanbanBtn.setIcon(FontAwesome.TH);

        Button roadmapBtn = new Button("List", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent clickEvent) {
                EventBusFactory.getInstance().post(new MilestoneEvent.GotoRoadmap(MilestoneListViewImpl.this));
            }
        });
        roadmapBtn.setDescription("Roadmap");
        roadmapBtn.setIcon(VaadinIcons.CUBE);

        ToggleButtonGroup viewButtons = new ToggleButtonGroup();
        viewButtons.addButton(roadmapBtn);
        viewButtons.addButton(kanbanBtn);
        viewButtons.withDefaultButton(kanbanBtn);
        layout.with(viewButtons);

        return layout;
    }

    private void constructBody() {
        CustomLayout bodyContent = CustomLayoutExt.createLayout("milestoneView");
        bodyContent.setWidth("100%");
        bodyContent.setStyleName("milestone-view");

        MHorizontalLayout closedHeaderLayout = new MHorizontalLayout();

        closedHeader = new Label("", ContentMode.HTML);
        closedHeader.setSizeUndefined();
        closedHeaderLayout.with(closedHeader).withAlign(closedHeader, Alignment.MIDDLE_CENTER);

        bodyContent.addComponent(closedHeaderLayout, "closed-header");
        closeContainer = new CssLayout();
        closeContainer.setStyleName("milestone-col");
        closeContainer.setWidth("100%");
        bodyContent.addComponent(closeContainer, "closed-milestones");

        MHorizontalLayout inProgressHeaderLayout = new MHorizontalLayout();
        inProgressHeader = new Label("", ContentMode.HTML);
        inProgressHeader.setSizeUndefined();
        inProgressHeaderLayout.addComponent(inProgressHeader);
        inProgressHeaderLayout.setComponentAlignment(inProgressHeader, Alignment.MIDDLE_CENTER);

        bodyContent.addComponent(inProgressHeaderLayout, "in-progress-header");
        inProgressContainer = new CssLayout();
        inProgressContainer.setStyleName("milestone-col");
        inProgressContainer.setWidth("100%");
        bodyContent.addComponent(this.inProgressContainer, "in-progress-milestones");

        MHorizontalLayout futureHeaderLayout = new MHorizontalLayout();
        futureHeader = new Label("", ContentMode.HTML);
        futureHeader.setSizeUndefined();
        futureHeaderLayout.addComponent(futureHeader);
        futureHeaderLayout.setComponentAlignment(futureHeader, Alignment.MIDDLE_CENTER);

        bodyContent.addComponent(futureHeaderLayout, "future-header");
        futureContainer = new CssLayout();
        futureContainer.setStyleName("milestone-col");
        futureContainer.setWidth("100%");
        bodyContent.addComponent(this.futureContainer, "future-milestones");

        this.addComponent(bodyContent);
    }

    private StreamResource buildStreamSource(ReportExportType exportType) {
        List fields = Arrays.asList(MilestoneTableFieldDef.milestoneName(), MilestoneTableFieldDef.status(),
                MilestoneTableFieldDef.startDate(), MilestoneTableFieldDef.endDate(), MilestoneTableFieldDef.id(),
                MilestoneTableFieldDef.assignee());
        SimpleReportTemplateExecutor reportTemplateExecutor = new SimpleReportTemplateExecutor.AllItems<>("Milestones",
                new RpFieldsBuilder(fields), exportType, SimpleMilestone.class, AppContextUtil.getSpringBean
                (MilestoneService.class));
        ReportStreamSource streamSource = new ReportStreamSource(reportTemplateExecutor) {
            @Override
            protected void initReportParameters(Map<String, Object> parameters) {
                MilestoneSearchCriteria searchCriteria = new MilestoneSearchCriteria();
                searchCriteria.setProjectIds(new SetSearchField<>(CurrentProjectVariables.getProjectId()));
                parameters.put(SimpleReportTemplateExecutor.CRITERIA, searchCriteria);
            }
        };
        return new StreamResource(streamSource, exportType.getDefaultFileName());
    }

    private void updateClosedMilestoneNumber(int closeMilestones) {
        closedHeader.setValue(FontAwesome.MINUS_CIRCLE.getHtml() + " " +
                AppContext.getMessage(MilestoneI18nEnum.WIDGET_CLOSED_PHASE_TITLE) + " (" + closeMilestones + ")");
    }

    private void updateFutureMilestoneNumber(int futureMilestones) {
        futureHeader.setValue(FontAwesome.CLOCK_O.getHtml() + " " +
                AppContext.getMessage(MilestoneI18nEnum.WIDGET_FUTURE_PHASE_TITLE) + " (" + futureMilestones + ")");
    }

    private void updateInProgressMilestoneNumber(int inProgressMilestones) {
        inProgressHeader.setValue(FontAwesome.SPINNER.getHtml() + " " +
                AppContext.getMessage(MilestoneI18nEnum.WIDGET_INPROGRESS_PHASE_TITLE) + " (" + inProgressMilestones +
                ")");
    }

    private ComponentContainer constructMilestoneBox(final SimpleMilestone milestone) {
        return new MilestoneBox(milestone);
    }

    private class MilestoneBox extends CssLayout {
        MilestoneBox(final SimpleMilestone milestone) {
            this.addStyleName(UIConstants.MILESTONE_BOX);
            this.setWidth("100%");

            ToggleMilestoneSummaryField toggleMilestoneSummaryField = new ToggleMilestoneSummaryField(milestone, 50);

            MHorizontalLayout milestoneHeader = new MHorizontalLayout().withFullWidth()
                    .with(toggleMilestoneSummaryField).expand(toggleMilestoneSummaryField);

            PopupButton taskSettingPopupBtn = new PopupButton();
            taskSettingPopupBtn.setWidth("15px");
            OptionPopupContent filterBtnLayout = new OptionPopupContent();

            Button editButton = new Button(AppContext.getMessage(GenericI18Enum.BUTTON_EDIT), new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {
                    EventBusFactory.getInstance().post(new MilestoneEvent.GotoEdit(MilestoneBox.this, milestone));
                }
            });
            editButton.setEnabled(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.MILESTONES));
            editButton.setIcon(FontAwesome.EDIT);
            filterBtnLayout.addOption(editButton);

            Button deleteBtn = new Button(AppContext.getMessage(GenericI18Enum.BUTTON_DELETE), new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {
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
                                        MilestoneService projectTaskService = AppContextUtil.getSpringBean(MilestoneService.class);
                                        projectTaskService.removeWithSession(milestone, AppContext.getUsername(), AppContext.getAccountId());
                                        displayMilestones();
                                    }
                                }
                            });
                }
            });
            deleteBtn.setIcon(FontAwesome.TRASH_O);
            deleteBtn.setEnabled(CurrentProjectVariables.canAccess(ProjectRolePermissionCollections.MILESTONES));
            filterBtnLayout.addDangerOption(deleteBtn);

            taskSettingPopupBtn.setIcon(FontAwesome.COG);
            taskSettingPopupBtn.addStyleName(UIConstants.BUTTON_ICON_ONLY);
            taskSettingPopupBtn.setContent(filterBtnLayout);

            milestoneHeader.addComponent(taskSettingPopupBtn);
            this.addComponent(milestoneHeader);

            int openAssignments = milestone.getNumOpenBugs() + milestone.getNumOpenTasks();
            int totalAssignments = milestone.getNumBugs() + milestone.getNumTasks();
            ELabel progressInfoLbl;
            if (totalAssignments > 0) {
                progressInfoLbl = new ELabel(AppContext.getMessage(ProjectI18nEnum.OPT_PROJECT_ASSIGNMENT,
                        (totalAssignments - openAssignments), totalAssignments, (totalAssignments - openAssignments)
                                * 100 / totalAssignments)).withStyleName(UIConstants.META_INFO);
            } else {
                progressInfoLbl = new ELabel(AppContext.getMessage(ProjectI18nEnum.OPT_NO_ASSIGNMENT))
                        .withStyleName(UIConstants.META_INFO);
            }
            this.addComponent(progressInfoLbl);

            CssLayout metaBlock = new CssLayout();
            MilestonePopupFieldFactory popupFieldFactory = ViewManager.getCacheComponent(MilestonePopupFieldFactory.class);
            metaBlock.addComponent(popupFieldFactory.createMilestoneAssigneePopupField(milestone, false));
            metaBlock.addComponent(popupFieldFactory.createStartDatePopupField(milestone));
            metaBlock.addComponent(popupFieldFactory.createEndDatePopupField(milestone));
            if (!SiteConfiguration.isCommunityEdition()) {
                metaBlock.addComponent(popupFieldFactory.createBillableHoursPopupField(milestone));
                metaBlock.addComponent(popupFieldFactory.createNonBillableHoursPopupField(milestone));
            }

            this.addComponent(metaBlock);
        }
    }
}
