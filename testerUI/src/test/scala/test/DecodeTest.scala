package test

import clientRequests.PartialCourseDataResponse
import io.circe.Decoder

object DecodeTest{
  def main(args: Array[String]): Unit = {
    import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*
    implicit val dec: Decoder[PartialCourseDataResponse] = clientRequests.PartialCourseDataJson.resDec
    import io.circe.parser.decode

    println(decode[PartialCourseDataResponse](s))
  }
  var s =
    """{
      |    "PartialCourseDataSuccess": {
      |        "course": {
      |            "courseId": "1",
      |            "title": "8 класс математики",
      |            "description": "\n",
      |            "status": {
      |                "Passing": {
      |                    "endsAt": null
      |                }
      |            },
      |            "courseData": {
      |                "title": "8 класс математики",
      |                "annotation": "",
      |                "childs": [
      |                    {
      |
      |                            "Theme": {
      |                                "alias": "firstQuadrum",
      |                                "title": "Первая четверть",
      |                                "textHtml": "",
      |                                "childs": [
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "dataStructuresHashTable",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    },
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "javaCourseSubstringSearchBaseRabinKarp",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    },
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "javaCourseSubstringSearchFiniteStateMachine",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    },
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "javaCourseSubstringSearchBaseKnuthMorrisPratt",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    },
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "javaCourseSubstringSearchBaseBoyerMoore",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    },
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "myCourses.g8_20_21.SubstringSearchCompare",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    },
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "algorithmsSubstringsCount",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    },
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "algorithmsManacherAlgo",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    }
      |                                ],
      |                                "displayMe": {
      |                                    "OwnPage": {}
      |                                }
      |                            }
      |                        }
      |                    ,
      |                    {
      |
      |                            "Theme": {
      |                                "alias": "secondQuadrum",
      |                                "title": "Вторая четверть",
      |                                "textHtml": "",
      |                                "childs": [
      |                                    {
      |
      |                                            "Theme": {
      |                                                "alias": "geometry",
      |                                                "title": "Вычислительная геометрия",
      |                                                "textHtml": "",
      |                                                "childs": [
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "algorithmstriangleArea",
      |                                                            "displayMe": {
      |                                                                "OwnPage": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "algorithmsPointInTriangle",
      |                                                            "displayMe": {
      |                                                                "OwnPage": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "algorithmsSegmentIntersection",
      |                                                            "displayMe": {
      |                                                                "OwnPage": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "algorithmsDistanceFromPointToLine",
      |                                                            "displayMe": {
      |                                                                "OwnPage": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "algorithmsLineToLineIntersection",
      |                                                            "displayMe": {
      |                                                                "OwnPage": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "algorithmsNangleArea",
      |                                                            "displayMe": {
      |                                                                "OwnPage": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "algorithmsRectangleAnotherPoint",
      |                                                            "displayMe": {
      |                                                                "OwnPage": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "algorithmsSurveillanceCamera",
      |                                                            "displayMe": {
      |                                                                "OwnPage": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "algorithmsConvexHull",
      |                                                            "displayMe": {
      |                                                                "OwnPage": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    }
      |                                                ],
      |                                                "displayMe": {
      |                                                    "OwnPage": {}
      |                                                }
      |                                            }
      |
      |                                    },
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "algorithmsGaussMethod",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    }
      |                                ],
      |                                "displayMe": {
      |                                    "OwnPage": {}
      |                                }
      |                            }
      |
      |                    },
      |                    {
      |
      |                            "Theme": {
      |                                "alias": "thirdQuadrum",
      |                                "title": "Третья четверть",
      |                                "textHtml": "",
      |                                "childs": [
      |                                    {
      |                                        "Problem": {
      |                                            "problemAlias": "algorithmsSpatialPartitioning",
      |                                            "displayMe": {
      |                                                "OwnPage": {}
      |                                            },
      |                                            "displayInContentsHtml": null
      |                                        }
      |                                    },
      |                                    {
      |
      |                                            "Theme": {
      |                                                "alias": "visibilityGraph",
      |                                                "title": "Поик пути. Граф видимости.",
      |                                                "textHtml": "<div>Цель проекта - написать алгоритм позволяющий найти кратчашиуий путь, для объекта прозвольной формы, между двумя точками, препятствия представленны выпуклыми многоугольниками.<a href=\"https://pastebin.com/Fpi7LkFw\">Код для рисования графики который вам пригодится.</a></div>",
      |                                                "childs": [
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "projectsVisibilityGraph.DrawVisibilityGraph",
      |                                                            "displayMe": {
      |                                                                "Inline": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "projectsVisibilityGraph.BuildVisibilityGraph",
      |                                                            "displayMe": {
      |                                                                "Inline": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    },
      |                                                    {
      |                                                        "Problem": {
      |                                                            "problemAlias": "projectsVisibilityGraph.FindShortestPath",
      |                                                            "displayMe": {
      |                                                                "Inline": {}
      |                                                            },
      |                                                            "displayInContentsHtml": null
      |                                                        }
      |                                                    }
      |                                                ],
      |                                                "displayMe": {
      |                                                    "OwnPage": {}
      |                                                }
      |                                            }
      |
      |                                    }
      |                                ],
      |                                "displayMe": {
      |                                    "OwnPage": {}
      |                                }
      |                            }
      |                        }
      |                     ]
      |            },
      |            "problems": [
      |                {
      |                    "problemId": "7",
      |                    "templateAlias": "dataStructuresHashTable",
      |                    "title": "Хеш-таблицы",
      |                    "score": {
      |                        "MultipleRunsResultScore": {
      |                            "runResults": [
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 87,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 77,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 89,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 83,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 84,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 104,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 111,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 113,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 157,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 163,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 249,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 435,
      |                                        "message": null
      |                                    }
      |                                }
      |                            ]
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "477",
      |                    "templateAlias": "javaCourseSubstringSearchBaseKnuthMorrisPratt",
      |                    "title": "Алгоритм Кнута — Морриса — Пратта",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "478",
      |                    "templateAlias": "javaCourseSubstringSearchBaseBoyerMoore",
      |                    "title": "Алгоритм Бойера — Мура",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "479",
      |                    "templateAlias": "javaCourseSubstringSearchBaseRabinKarp",
      |                    "title": "Алгоритм Рабина — Карпа",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "480",
      |                    "templateAlias": "javaCourseSubstringSearchFiniteStateMachine",
      |                    "title": "Поиск подстроки используя конечные автоматы",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "719",
      |                    "templateAlias": "javaCourseSubstringSearchBaseKnuthMorrisPratt",
      |                    "title": "Алгоритм Кнута — Морриса — Пратта",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "720",
      |                    "templateAlias": "javaCourseSubstringSearchBaseBoyerMoore",
      |                    "title": "Алгоритм Бойера — Мура",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "721",
      |                    "templateAlias": "javaCourseSubstringSearchBaseRabinKarp",
      |                    "title": "Алгоритм Рабина — Карпа",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "730",
      |                    "templateAlias": "javaCourseSubstringSearchBaseKnuthMorrisPratt",
      |                    "title": "Алгоритм Кнута — Морриса — Пратта",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "731",
      |                    "templateAlias": "javaCourseSubstringSearchBaseBoyerMoore",
      |                    "title": "Алгоритм Бойера — Мура",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "931",
      |                    "templateAlias": "javaCourseSubstringSearchBaseKnuthMorrisPratt",
      |                    "title": "Алгоритм Кнута — Морриса — Пратта",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "1775",
      |                    "templateAlias": "myCourses.g8_20_21.SubstringSearchCompare",
      |                    "title": "Сравнение алгоритмов поиска строк",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "3134",
      |                    "templateAlias": "algorithmsSubstringsCount",
      |                    "title": "Количество подстрок",
      |                    "score": {
      |                        "MultipleRunsResultScore": {
      |                            "runResults": [
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 110,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 95,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 89,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 83,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 98,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 88,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 108,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 88,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 122,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 133,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 140,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 233,
      |                                        "message": null
      |                                    }
      |                                }
      |                            ]
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "3135",
      |                    "templateAlias": "algorithmsManacherAlgo",
      |                    "title": "Алгоритм манакера",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "3697",
      |                    "templateAlias": "algorithmstriangleArea",
      |                    "title": "Площадь треугольника",
      |                    "score": {
      |                        "MultipleRunsResultScore": {
      |                            "runResults": [
      |                                {
      |                                    "ProgramRunResultWrongAnswer": {
      |                                        "message": "Неверный ответ:\n\nВвод:\n0 0 1 1 2 2\nОжидалось:\n0.0\n"
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                }
      |                            ]
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "4103",
      |                    "templateAlias": "algorithmsNangleArea",
      |                    "title": "Площадь многоугольника",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "4104",
      |                    "templateAlias": "algorithmsSurveillanceCamera",
      |                    "title": "Камера наблюдения",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "4105",
      |                    "templateAlias": "algorithmsConvexHull",
      |                    "title": "Выпуклая оболочка",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "3907",
      |                    "templateAlias": "algorithmsDistanceFromPointToLine",
      |                    "title": "Ловля коня",
      |                    "score": {
      |                        "MultipleRunsResultScore": {
      |                            "runResults": [
      |                                {
      |                                    "ProgramRunResultWrongAnswer": {
      |                                        "message": "Неверный ответ:\n\nВвод:\n0.0 4.0 0.0 0.0 6.0 0.0\nОжидалось:\n6.0\n"
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                }
      |                            ]
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "3908",
      |                    "templateAlias": "algorithmsPointInTriangle",
      |                    "title": "Точка в треугольнике",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "3909",
      |                    "templateAlias": "algorithmsRectangleAnotherPoint",
      |                    "title": "4й угол",
      |                    "score": {
      |                        "MultipleRunsResultScore": {
      |                            "runResults": [
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 124,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 118,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 130,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 133,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 171,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 166,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 148,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 163,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 149,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 135,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 125,
      |                                        "message": null
      |                                    }
      |                                }
      |                            ]
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "3910",
      |                    "templateAlias": "algorithmsSegmentIntersection",
      |                    "title": "Гадание на спичках",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "4568",
      |                    "templateAlias": "algorithmsGaussMethod",
      |                    "title": "Метод Гауса",
      |                    "score": {
      |                        "MultipleRunsResultScore": {
      |                            "runResults": [
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 93,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 98,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 92,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 86,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 87,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 96,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 101,
      |                                        "message": null
      |                                    }
      |                                }
      |                            ]
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "4859",
      |                    "templateAlias": "algorithmsLineToLineIntersection",
      |                    "title": "Пересечение прямых",
      |                    "score": {
      |                        "MultipleRunsResultScore": {
      |                            "runResults": [
      |                                {
      |                                    "ProgramRunResultSuccess": {
      |                                        "timeMS": 114,
      |                                        "message": null
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultWrongAnswer": {
      |                                        "message": "Неверный ответ:\n0.0 0.0\n\nВвод:\n0.0 0.0 1.0 1.0 1.0 5.0 5.0 0.0\nОжидалось:\n5.0 5.0\n"
      |                                    }
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                },
      |                                {
      |                                    "ProgramRunResultNotTested": {}
      |                                }
      |                            ]
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "5766",
      |                    "templateAlias": "algorithmsSpatialPartitioning",
      |                    "title": "Точки в прямоугольнике",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "5988",
      |                    "templateAlias": "projectsVisibilityGraph.FindShortestPath",
      |                    "title": "Поиск пути",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "5989",
      |                    "templateAlias": "projectsVisibilityGraph.DrawVisibilityGraph",
      |                    "title": "Визуализация",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                },
      |                {
      |                    "problemId": "5990",
      |                    "templateAlias": "projectsVisibilityGraph.BuildVisibilityGraph",
      |                    "title": "Построение графа видимости",
      |                    "score": {
      |                        "BinaryScore": {
      |                            "passed": false
      |                        }
      |                    }
      |                }
      |            ]
      |        }
      |    }
      |}
      |""".stripMargin
}
