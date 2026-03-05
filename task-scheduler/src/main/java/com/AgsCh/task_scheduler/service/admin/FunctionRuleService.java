package com.AgsCh.task_scheduler.service.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.request.FunctionRuleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionRuleResponseDTO;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.FunctionRule;
import com.AgsCh.task_scheduler.model.RuleType;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.FunctionRuleRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;

@Service
public class FunctionRuleService {

        private final FunctionRuleRepository repository;
        private final FunctionRepository functionRepository;
        private final UserRepository userRepository;

        public FunctionRuleService(FunctionRuleRepository repository,
                        FunctionRepository functionRepository,
                        UserRepository userRepository) {
                this.repository = repository;
                this.functionRepository = functionRepository;
                this.userRepository = userRepository;
        }

        public Map<String, RuleType> buildRuleMap(Long houseId) {

                List<FunctionRule> rules = repository.findByHouseId(houseId);

                Map<String, RuleType> map = new HashMap<>();

                for (FunctionRule rule : rules) {
                        Long idA = rule.getFunctionA().getId();
                        Long idB = rule.getFunctionB().getId();

                        String key = buildKey(idA, idB);
                        map.put(key, rule.getType());
                }

                return map;
        }

        private String buildKey(Long a, Long b) {
                return a < b ? a + "-" + b : b + "-" + a;
        }

        public void create(FunctionRuleRequestDTO dto, String username) {

                User user = userRepository.findByUsername(username)
                                .orElseThrow();

                Function functionA = functionRepository.findById(dto.getFunctionAId())
                                .orElseThrow();

                Function functionB = functionRepository.findById(dto.getFunctionBId())
                                .orElseThrow();

                if (functionA.equals(functionB)) {
                        throw new IllegalArgumentException("No se puede crear regla con la misma función");
                }

                repository.save(new FunctionRule(
                                normalizeA(functionA, functionB),
                                normalizeB(functionA, functionB),
                                dto.getType(),
                                user.getHouse()));
        }

        private Function normalizeA(Function a, Function b) {
                return a.getId() < b.getId() ? a : b;
        }

        private Function normalizeB(Function a, Function b) {
                return a.getId() < b.getId() ? b : a;
        }

        public List<FunctionRuleResponseDTO> findByUser(String username) {

                User user = userRepository.findByUsername(username)
                                .orElseThrow();

                return repository.findByHouseId(user.getHouse().getId())
                                .stream()
                                .map(rule -> new FunctionRuleResponseDTO(
                                                rule.getId(),
                                                rule.getFunctionA().getId(),
                                                rule.getFunctionA().getName(),
                                                rule.getFunctionB().getId(),
                                                rule.getFunctionB().getName(),
                                                rule.getType()))
                                .toList();
        }

        public void delete(Long id, String username) {

                // 1️⃣ Buscar usuario que intenta borrar
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

                Long houseId = user.getHouse().getId();

                // 2️⃣ Verificar que la regla exista y pertenezca a su house
                FunctionRule rule = repository.findByIdAndHouse_Id(id, houseId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Regla no encontrada o no pertenece a tu house"));

                // 3️⃣ Eliminar
                repository.delete(rule);
        }

        public void update(Long id, FunctionRuleRequestDTO dto, String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

                Long houseId = user.getHouse().getId();

                FunctionRule rule = repository.findByIdAndHouse_Id(id, houseId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Regla no encontrada o no pertenece a tu house"));

                Function functionA = functionRepository.findById(dto.getFunctionAId())
                                .orElseThrow();
                Function functionB = functionRepository.findById(dto.getFunctionBId())
                                .orElseThrow();

                if (functionA.equals(functionB)) {
                        throw new IllegalArgumentException("No se puede asignar la misma función a A y B");
                }

                rule.setFunctionA(normalizeA(functionA, functionB));
                rule.setFunctionB(normalizeB(functionA, functionB));
                rule.setType(dto.getType());

                repository.save(rule);
        }

        public FunctionRuleResponseDTO findById(Long id, String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                Long houseId = user.getHouse().getId();

                FunctionRule rule = repository.findByIdAndHouse_Id(id, houseId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Regla no encontrada o no pertenece a tu house"));

                return new FunctionRuleResponseDTO(
                                rule.getId(),
                                rule.getFunctionA().getId(),
                                rule.getFunctionA().getName(),
                                rule.getFunctionB().getId(),
                                rule.getFunctionB().getName(),
                                rule.getType());
        }
}