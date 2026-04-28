export const OptionBuilder=(translationMap, valueList)=>{
    const option = [];
    
    valueList.forEach(element => {
        option.push({
            'label': translationMap[element],
            'value': element
        })
    });
    return option;
} 