package ca.corefacility.bioinformatics.irida.ria.web.announcements;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ca.corefacility.bioinformatics.irida.model.announcements.Announcement;
import ca.corefacility.bioinformatics.irida.model.announcements.AnnouncementUserJoin;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.repositories.specification.AnnouncementSpecification;
import ca.corefacility.bioinformatics.irida.repositories.specification.UserSpecification;
import ca.corefacility.bioinformatics.irida.ria.web.announcements.dto.AnnouncementRequest;
import ca.corefacility.bioinformatics.irida.ria.web.announcements.dto.AnnouncementTableModel;
import ca.corefacility.bioinformatics.irida.ria.web.announcements.dto.AnnouncementUser;
import ca.corefacility.bioinformatics.irida.ria.web.models.tables.TableRequest;
import ca.corefacility.bioinformatics.irida.ria.web.models.tables.TableResponse;
import ca.corefacility.bioinformatics.irida.service.AnnouncementService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;

/**
 * Controller for all ajax requests from the UI for announcements.
 */
@RestController
@RequestMapping("/ajax/announcements")
public class AnnouncementAjaxController {
	private final AnnouncementService announcementService;
	private final UserService userService;

	@Autowired
	public AnnouncementAjaxController(AnnouncementService announcementService, UserService userService) {
		this.announcementService = announcementService;
		this.userService = userService;
	}

	/**
	 * Returns a paged list of announcements for an administrator.
	 *
	 * @param tableRequest details about the current page of the table requested
	 * @return a {@link TableResponse} containing the list of announcements.
	 */
	@RequestMapping(value = "/control/list")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public @ResponseBody
	TableResponse getAnnouncementsAdmin(@RequestBody TableRequest tableRequest) {
		final Page<Announcement> page = announcementService.search(
				AnnouncementSpecification.searchAnnouncement(tableRequest.getSearch()),
				PageRequest.of(tableRequest.getCurrent(), tableRequest.getPageSize(), tableRequest.getSort()));
		long usersTotal = userService.count();

		final List<AnnouncementTableModel> announcements = page.getContent()
				.stream()
				.map(a -> new AnnouncementTableModel(a, usersTotal,
						announcementService.countReadsForOneAnnouncement(a)))
				.collect(Collectors.toList());
		return new TableResponse(announcements, page.getTotalElements());
	}

	/**
	 * Creates a new announcement
	 *
	 * @param announcementRequest details about the announcement to create.
	 * @param principal           the currently logged in user
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void createNewAnnouncement(@RequestBody AnnouncementRequest announcementRequest, Principal principal) {
		User user = userService.getUserByUsername(principal.getName());
		Announcement announcement = new Announcement(announcementRequest.getMessage(), user);
		announcementService.create(announcement);
	}

	/**
	 * Update an existing announcement
	 *
	 * @param announcementRequest - the details of the announcement to update.
	 */
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void updateAnnouncement(@RequestBody AnnouncementRequest announcementRequest) {
		Announcement announcement = announcementService.read(announcementRequest.getId());
		announcement.setMessage(announcementRequest.getMessage());
		announcementService.update(announcement);
	}

	/**
	 * Delete an existing announcement.
	 *
	 * @param announcementRequest - the announcement to delete
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void deleteAnnouncement(@RequestBody AnnouncementRequest announcementRequest) {
		announcementService.delete(announcementRequest.getId());
	}

	@RequestMapping("/details")
	public TableResponse getAnnouncementDetails(@RequestParam Long id, TableRequest tableRequest) {
		Announcement announcement = announcementService.read(id);
		List<AnnouncementUserJoin> readUsers = announcementService.getReadUsersForAnnouncement(announcement);

		Page<User> usersPage = userService.search(UserSpecification.searchUser(tableRequest.getSearch()),
				PageRequest.of(tableRequest.getCurrent(), tableRequest.getPageSize(), tableRequest.getSort()));
		List<AnnouncementUser> users = usersPage.getContent()
				.stream()
				.map(user -> {
					Optional<AnnouncementUserJoin> currentAnnouncement = readUsers.stream()
							.filter(j -> j.getObject()
									.equals(user))
							.findAny();
					Date readDate = currentAnnouncement.map(AnnouncementUserJoin::getCreatedDate)
							.orElse(null);
					return new AnnouncementUser(user, readDate);
				}).collect(Collectors.toList());
		return new TableResponse(users, usersPage.getTotalElements());
	}
}

