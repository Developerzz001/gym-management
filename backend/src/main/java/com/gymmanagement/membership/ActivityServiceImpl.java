package com.gymmanagement.membership;

import com.gymmanagement.common.exception.DuplicateResourceException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.membership.dto.ActivityRequest;
import com.gymmanagement.membership.dto.ActivityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public ActivityResponse create(ActivityRequest request) {
        if (activityRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new DuplicateResourceException("Sport/Activity name already exists: " + request.getName());
        }
        Activity activity = Activity.builder()
                .name(request.getName().trim())
                .description(trim(request.getDescription()))
                .build();
        return activityMapper.toResponse(activityRepository.save(activity));
    }

    @Override
    @Transactional
    public ActivityResponse update(Long id, ActivityRequest request) {
        Activity activity = getEntityById(id);
        if (activityRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw new DuplicateResourceException("Sport/Activity name already exists: " + request.getName());
        }
        activity.setName(request.getName().trim());
        activity.setDescription(trim(request.getDescription()));
        return activityMapper.toResponse(activityRepository.save(activity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        activityRepository.delete(getEntityById(id));
    }

    @Override
    public List<ActivityResponse> getAll() {
        return activityRepository.findAll(Sort.by("name").ascending()).stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Override
    public Activity getEntityById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sport/Activity", "id", id));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
