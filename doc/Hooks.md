# Hooks

This document describes how the hook mechanism works.
Each hook calls it's library method and gets a list of commands in return.

## Overview
| hook            | description |
| ---------------- | ----------- |
| onBegin          | **Deprecated**, instantiation of a new coach |
| onInit           | Instantiation of a new coach |
| onResume         | Resume of an instantiated coach |
| onBeginBlock     | **Deprecated**, Beginning of a new block |
| onBlockLoad      | Beginning of a new block |
| onQuestionLoad   | **Not impl**, Loading of a question, before rendering |
| onResponseChange | Submitting of an answer |
| onUnload         | **Not impl**, Unloading of a coach |

## API

### onBegin
```
List<Command> onBegin();
```

### onResume
```
List<Command> onResume(String questionId);
```

### onBeginBlock
```
List<Command> onBeginBlock(String blockId);
```

### onResponseChange
```
List<Command> onResponseChange(Question question, Answer answer,
                               Block block, Questionnaire questionnaire);
```
  