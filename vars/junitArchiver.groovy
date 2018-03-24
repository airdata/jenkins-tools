def archive(resultsPattern) {
    step([$class: 'JUnitResultArchiver', testResults: "${resultsPattern}"])
}