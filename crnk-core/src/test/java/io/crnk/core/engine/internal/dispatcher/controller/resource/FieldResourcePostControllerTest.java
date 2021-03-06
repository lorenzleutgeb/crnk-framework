package io.crnk.core.engine.internal.dispatcher.controller.resource;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.FieldResourcePost;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePostController;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldResourcePostControllerTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = HttpMethod.POST.name();

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/project");
		FieldResourcePost sut = new FieldResourcePost();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onRelationshipRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/relationships/project");
		FieldResourcePost sut = new FieldResourcePost();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks");
		FieldResourcePost sut = new FieldResourcePost();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onExistingParentResourceShouldSaveIt() {
		// GIVEN
		Document newTaskDocument = new Document();
		newTaskDocument.setData(Nullable.of(createTask()));

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);

		// WHEN
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskDocument);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectDocument = new Document();
		newProjectDocument.setData(Nullable.of(createProject()));

		JsonPath projectPath = pathBuilder.build("/tasks/" + taskId + "/project");
		FieldResourcePost sut = new FieldResourcePost();
		sut.init(controllerContext);

		// WHEN
		Response projectResponse = sut.handle(projectPath, emptyProjectQuery, newProjectDocument);

		// THEN
		assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "project", new QueryParams());
		assertThat(project.getId()).isEqualTo(projectId);
	}

	@Test
	public void onExistingParentResourceShouldSaveToToMany() {
		// GIVEN
		Document newTaskDocument = new Document();
		newTaskDocument.setData(Nullable.of(createTask()));

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);

		// WHEN
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskDocument);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectDocument = new Document();
		newProjectDocument.setData(Nullable.of(createProject()));

		JsonPath projectPath = pathBuilder.build("/tasks/" + taskId + "/projects");
		FieldResourcePost sut = new FieldResourcePost();
		sut.init(controllerContext);

		// WHEN
		Response projectResponse = sut.handle(projectPath, emptyProjectQuery, newProjectDocument);

		// THEN
		assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "projects", new QueryParams());
		assertThat(project.getId()).isEqualTo(projectId);
	}
}
